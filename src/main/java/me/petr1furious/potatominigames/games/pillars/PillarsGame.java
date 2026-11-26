/*
 * Based on PillarPeril by MarcPG (https://github.com/MarcPG1905/PillarPeril, Apache-2.0),
 * via the fork at https://github.com/Petr1Furious/PillarPeril. Rewritten in Java for
 * Potato Minigames by Claude, and modified; see NOTICE.
 */
package me.petr1furious.potatominigames.games.pillars;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.core.command.GameCommands;
import me.petr1furious.potatominigames.core.command.Suggest;
import me.petr1furious.potatominigames.core.config.ConfigRegistry;
import me.petr1furious.potatominigames.core.game.Ballot;
import me.petr1furious.potatominigames.core.game.GameSession;
import me.petr1furious.potatominigames.core.game.SessionServices;
import me.petr1furious.potatominigames.core.game.StartOption;
import me.petr1furious.potatominigames.core.preset.GamePreset;
import me.petr1furious.potatominigames.core.preset.PresetGame;
import me.petr1furious.potatominigames.core.preset.PresetStore;
import me.petr1furious.potatominigames.core.queue.QueueService;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.core.world.SlimeWorldService;
import me.petr1furious.potatominigames.games.pillars.PillarsPresets.Settings;
import me.petr1furious.potatominigames.i18n.I18n;

import static me.petr1furious.potatominigames.core.command.GameCommands.requirePlayer;
import static me.petr1furious.potatominigames.core.command.GameCommands.run;

public final class PillarsGame implements PresetGame, Listener {
    private static final List<String> TIME_SUGGESTIONS = List.of("30s", "1min", "2min", "5min", "10min", "30min");

    private final JavaPlugin plugin;
    private final PillarsConfig config;
    private final SlimeWorldService slimeWorlds;
    private final ConfigRegistry configs;
    private final PresetStore<Settings> presets;
    private final List<Ballot> partyBallots;
    private final List<Ballot> casinoBallots;
    private List<String> modeIds;
    private QueueService queues;
    private PillarsSession activeSession;

    public PillarsGame(JavaPlugin plugin, ConfigRegistry configs, SlimeWorldService slimeWorlds) {
        this.plugin = plugin;
        this.config = new PillarsConfig(configs);
        this.partyBallots = variantBallot("party", List.of(Material.NOTE_BLOCK, Material.JUKEBOX));
        this.casinoBallots = variantBallot("casino", List.of(Material.EMERALD, Material.GOLD_INGOT, Material.REDSTONE));
        this.slimeWorlds = slimeWorlds;
        this.configs = configs;
        try {
            this.presets = PillarsPresets.open(plugin.getDataFolder().toPath().resolve("data/pillars-presets.yml"),
                    PillarsConfig.RESERVED);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load Pillars presets; existing data has been left untouched.",
                    exception);
        }
        refreshPresetModes();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setQueues(QueueService queues) {
        this.queues = queues;
    }

    private void refreshPresetModes() {
        List<String> modes = new ArrayList<>(PillarsConfig.MODES);
        modes.addAll(presets.names());
        modeIds = List.copyOf(modes);
        if (queues != null) {
            queues.refreshQueues(this, configs);
        }
    }

    void setActiveSession(PillarsSession session) {
        this.activeSession = session;
    }

    void clearActiveSession(PillarsSession session) {
        if (activeSession == session) {
            activeSession = null;
        }
    }

    @Override
    public String id() {
        return "pillars";
    }

    @Override
    public Component displayName() {
        return I18n.tr("pillars.play.name").color(NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public List<String> modes() {
        return modeIds;
    }

    @Override
    public String defaultMode() {
        String mode = PillarsConfig.canonicalMode(config.mode.get());
        return PillarsConfig.MODES.contains(mode) ? mode : "original";
    }

    @Override
    public List<String> classicModes() {
        return PillarsConfig.MODES;
    }

    @Override
    public List<GamePreset<Settings>> presets() {
        return presets.all();
    }

    @Override
    public void openPresetEditor(Player player, SessionManager sessions, Runnable back) {
        new PillarsPresetEditor(this, config, sessions, back).open(player);
    }

    @Override
    public List<Ballot> ballots(String mode) {
        return switch (mode) {
            case "party" -> partyBallots;
            case "casino" -> casinoBallots;
            default -> List.of();
        };
    }

    private List<Ballot> variantBallot(String mode, List<Material> icons) {
        List<String> variants = PillarsConfig.variants(mode);
        return List.of(new Ballot(PillarsConfig.VARIANT_OPTION, "Variant", () -> variants,
                () -> config.defaultVariant(mode), icons));
    }

    @Override
    public Material icon() {
        return Material.BEDROCK;
    }

    @Override
    public Component modeTitle(String mode) {
        if (PillarsConfig.RESERVED.contains(mode)) return I18n.tr("pillars.play.mode." + mode);
        var preset = presets.find(mode);
        return Component.text(preset == null ? mode : preset.name());
    }

    Component variantTitle(String variant) {
        return variant.equals("casino") ? I18n.tr("pillars.play.editor.neutral_casino") : modeTitle(variant);
    }

    Component modifierLabels(List<String> modifiers) {
        return modifiers.isEmpty() ? I18n.tr("pillars.play.modifiers.plain")
                : Component.join(net.kyori.adventure.text.JoinConfiguration.separator(Component.text(" + ")),
                        modifiers.stream().map(modifier -> modeTitle(modifier.equals("shuffle") ? "item-shuffle" : modifier)).toList());
    }
    @Override public Material icon(String mode) { return mode.equals("perks") ? Material.FEATHER : icon(); }

    @Override
    public Component description(String mode) {
        var preset = presets.find(mode);
        if (preset != null) {
            return I18n.tr("pillars.play.description.preset", preset.ownerName(),
                    modifierLabels(preset.settings().legacyModifiers()));
        }
        return I18n.tr("pillars.play.description." + (PillarsConfig.MODES.contains(mode) ? mode : "original"));
    }

    @Override
    public int minPlayers(String mode) {
        return 2;
    }

    @Override
    public int maxPlayers(String mode) {
        return 16;
    }

    @Override
    public GameSession createSession(SessionServices services, String mode) {
        var preset = presets.find(mode);
        return new PillarsSession(services, this, config, mode, preset == null ? null : preset.settings());
    }

    @Override
    public StartOption startOption() {
        return new StartOption(PillarsSession.ARENA_OPTION, () -> importedArenas().stream()
                .map(PillarsArena::name).toList());
    }

    List<PillarsArena> importedArenas() {
        return config.arenas().stream()
                .filter(arena -> slimeWorlds.templateExists(arena.template()))
                .toList();
    }

    private boolean inGame(Player player) {
        return activeSession != null && activeSession.isParticipant(player);
    }

    /** Beds and anchors are random items here; they must not move spawns. */
    @EventHandler(ignoreCancelled = true)
    public void onSetSpawn(PlayerSetSpawnEvent event) {
        if ((event.getCause() == PlayerSetSpawnEvent.Cause.BED
                || event.getCause() == PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) && inGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /** Lowest, so lucky blocks are not opened before the start either. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreakBeforeStart(BlockBreakEvent event) {
        if (activeSession != null && !activeSession.hasStarted()
                && activeSession.playsIn(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (inGame(event.getPlayer())) {
            event.setRespawnLocation(activeSession.respawnPoint());
            activeSession.onRespawn(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEndPortalFrame(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.END_PORTAL_FRAME && activeSession != null
                && activeSession.playsIn(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE && activeSession != null
                && activeSession.playsIn(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalReady(EntityPortalReadyEvent event) {
        if (event.getEntity() instanceof Player player && inGame(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        if (inGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private PillarsSession requireSession() throws CommandFailure {
        if (activeSession == null) {
            throw new CommandFailure(I18n.tr("pillars.play.error.no_session"));
        }
        return activeSession;
    }

    private static boolean isAdmin(CommandSourceStack source) {
        return source.getSender().hasPermission(GameCommands.ADMIN_PERMISSION);
    }

    @Override
    public void extendCommands(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("preset")
                .executes(ctx -> run(ctx, this::listPresets))
                .then(Commands.literal("list").executes(ctx -> run(ctx, this::listPresets)))
                .then(Commands.literal("save")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> run(ctx, c -> savePreset(c, List.of())))
                                .then(Commands.argument("modifiers", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> {
                                            int lastWord = builder.getInput().lastIndexOf(' ') + 1;
                                            return Suggest.strings(PillarsPresets.COMMAND_MODIFIERS)
                                                    .getSuggestions(ctx, builder.createOffset(lastWord));
                                        })
                                        .executes(ctx -> run(ctx, c -> savePreset(c,
                                                List.of(c.getArgument("modifiers", String.class).trim()
                                                        .split("\\s+"))))))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> Suggest.strings(presets.all().stream()
                                        .filter(preset -> ctx.getSource().getExecutor() instanceof Player player
                                                && preset.owner().equals(player.getUniqueId()))
                                        .map(GamePreset::id).toList()).getSuggestions(ctx, builder))
                                .executes(ctx -> run(ctx, this::deletePreset)))));

        root.then(Commands.literal("perk")
                .requires(PillarsGame::isAdmin)
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests(Suggest.strings(java.util.Arrays.stream(PerkType.values()).map(PerkType::id).toList()))
                        .executes(ctx -> run(ctx, c -> {
                            if (!requireSession().givePerk(requirePlayer(c), c.getArgument("type", String.class)))
                                throw new CommandFailure(I18n.tr("pillars.play.error.perk_unavailable"));
                        }))));

        root.then(Commands.literal("respawn")
                .requires(PillarsGame::isAdmin)
                .executes(ctx -> run(ctx, c -> respawn(c, requirePlayer(c))))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> run(ctx, c -> respawn(c, c.getArgument("player",
                                PlayerSelectorArgumentResolver.class).resolve(c.getSource()).getFirst())))));

        var time = Commands.literal("time").requires(PillarsGame::isAdmin);
        for (String operation : List.of("add", "remove", "set")) {
            time.then(Commands.literal(operation)
                    .then(Commands.argument("time", StringArgumentType.word())
                            .suggests(Suggest.strings(TIME_SUGGESTIONS))
                            .executes(ctx -> run(ctx, c -> changeTime(c, operation)))));
        }
        root.then(time);

        root.then(Commands.literal("info")
                .requires(PillarsGame::isAdmin)
                .executes(ctx -> run(ctx, c -> {
                    for (Component line : requireSession().info()) {
                        c.getSource().getSender().sendMessage(line);
                    }
                })));

        root.then(Commands.literal("arenas")
                .requires(PillarsGame::isAdmin)
                .executes(ctx -> run(ctx, this::listArenas)));

        root.then(Commands.literal("arena")
                .requires(PillarsGame::isAdmin)
                .then(Commands.literal("import")
                        .then(Commands.argument("arena", StringArgumentType.word())
                                .suggests(Suggest.dynamic(() -> {
                                    List<String> names = new java.util.ArrayList<>(List.of("all"));
                                    config.arenas().forEach(arena -> names.add(arena.name()));
                                    return names;
                                }))
                                .executes(ctx -> run(ctx, c -> importArenas(c, "pillars", 64)))
                                .then(Commands.argument("world", StringArgumentType.word())
                                        .executes(ctx -> run(ctx, c -> importArenas(c,
                                                c.getArgument("world", String.class), 64)))
                                        .then(Commands.argument("radius", IntegerArgumentType.integer(16, 512))
                                                .executes(ctx -> run(ctx, c -> importArenas(c,
                                                        c.getArgument("world", String.class),
                                                        c.getArgument("radius", Integer.class)))))))));
    }

    private void listPresets(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage(I18n.tr("pillars.play.presets.help", PresetStore.LIMIT).color(NamedTextColor.GOLD));
        sender.sendMessage(I18n.tr("pillars.play.presets.modifiers", String.join(", ", PillarsPresets.COMMAND_MODIFIERS))
                .color(NamedTextColor.GRAY));
        for (GamePreset<Settings> preset : presets.all()) {
            String command = "/play pillars " + preset.id();
            sender.sendMessage(I18n.tr("pillars.play.presets.entry", preset.name(), preset.ownerName(),
                    modifierLabels(preset.settings().legacyModifiers()), command).color(NamedTextColor.AQUA)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command)));
        }
    }

    private void requirePresetIdle(String name) throws CommandFailure {
        var preset = presets.find(name.strip());
        if (preset != null && queues != null && queues.isModeInUse(this, preset.id())) {
            throw new CommandFailure(I18n.tr("pillars.play.error.preset_busy"));
        }
    }

    private void savePreset(CommandContext<CommandSourceStack> ctx, List<String> modifiers) throws CommandFailure {
        Player player = requirePlayer(ctx);
        String name = ctx.getArgument("name", String.class);
        var previous = presets.find(name);
        try {
            savePreset(player, name, previous == null ? GamePreset.DEFAULT_ICON : previous.icon(),
                    PillarsPresets.modifiers(modifiers));
        } catch (IllegalArgumentException exception) {
            throw new CommandFailure(I18n.message(exception));
        }
    }

    void savePreset(Player player, String name, String icon, Settings settings) throws CommandFailure {
        requirePresetIdle(name);
        try {
            var preset = presets.save(player.getUniqueId(), player.getName(), name, icon, settings);
            refreshPresetModes();
            String command = "/play pillars " + preset.id();
            player.sendMessage(I18n.tr("pillars.play.presets.saved", preset.name(),
                    modifierLabels(preset.settings().legacyModifiers()), command).color(NamedTextColor.GREEN)
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command)));
        } catch (IllegalArgumentException exception) {
            throw new CommandFailure(I18n.message(exception));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not save Pillars preset", exception);
            throw new CommandFailure(I18n.tr("pillars.play.error.preset_save"));
        }
    }

    private void deletePreset(CommandContext<CommandSourceStack> ctx) throws CommandFailure {
        deletePreset(requirePlayer(ctx), ctx.getArgument("name", String.class));
    }

    @Override
    public void deletePreset(Player player, String name) throws CommandFailure {
        requirePresetIdle(name);
        try {
            presets.delete(player.getUniqueId(), name);
            refreshPresetModes();
            player.sendMessage(I18n.tr("pillars.play.presets.deleted", name).color(NamedTextColor.GREEN));
        } catch (IllegalArgumentException exception) {
            throw new CommandFailure(I18n.message(exception));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not delete Pillars preset", exception);
            throw new CommandFailure(I18n.tr("pillars.play.error.preset_delete"));
        }
    }

    private void respawn(CommandContext<CommandSourceStack> ctx, Player target) throws CommandFailure {
        if (!requireSession().respawn(target)) {
            throw new CommandFailure(I18n.tr("pillars.play.error.respawn", target.getName()));
        }
        ctx.getSource().getSender().sendMessage(
                I18n.tr("pillars.play.command.respawned", target.getName()).color(NamedTextColor.GREEN));
    }

    private void changeTime(CommandContext<CommandSourceStack> ctx, String operation) throws CommandFailure {
        PillarsSession session = requireSession();
        String raw = ctx.getArgument("time", String.class);
        Long seconds = PillarsConfig.parseDuration(raw);
        if (seconds == null) {
            throw new CommandFailure(I18n.tr("pillars.play.error.time", raw));
        }
        switch (operation) {
            case "add" -> session.addTime(seconds);
            case "remove" -> session.removeTime(seconds);
            default -> session.setTime(seconds);
        }
        ctx.getSource().getSender().sendMessage(I18n.tr("pillars.play.command.time_left",
                me.petr1furious.potatominigames.core.text.TimeFormat.words(session.timeLeft())).color(NamedTextColor.GREEN));
    }

    private void listArenas(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        List<PillarsArena> arenas = config.arenas();
        if (arenas.isEmpty()) {
            sender.sendMessage(I18n.tr("pillars.play.arena.none").color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(I18n.tr("pillars.play.arena.header").color(NamedTextColor.GRAY));
        for (PillarsArena arena : arenas) {
            boolean ready = slimeWorlds.templateExists(arena.template());
            Component details = I18n.tr("pillars.play.arena.entry",
                    Component.text(arena.name(), ready ? NamedTextColor.GREEN : NamedTextColor.RED),
                    arena.template(), arena.x(), arena.z()).color(NamedTextColor.GRAY);
            if (!arena.gameRules().isEmpty()) {
                details = details.append(I18n.tr("pillars.play.arena.rules", arena.gameRules().toString()));
            }
            sender.sendMessage(details);
        }
    }

    private void importArenas(CommandContext<CommandSourceStack> ctx, String worldName, int radius)
            throws CommandFailure {
        CommandSender sender = ctx.getSource().getSender();
        String which = ctx.getArgument("arena", String.class);
        List<PillarsArena> arenas = config.arenas().stream()
                .filter(arena -> which.equalsIgnoreCase("all") || arena.name().equalsIgnoreCase(which))
                .toList();
        if (arenas.isEmpty()) {
            throw new CommandFailure(I18n.tr("pillars.play.error.arena", which));
        }
        File source = worldFolder(worldName);
        if (source == null) {
            throw new CommandFailure(I18n.tr("pillars.play.error.world", worldName));
        }
        int spawnY = (int) Math.floor(config.platformHeight.get()) + 1;
        for (PillarsArena arena : arenas) {
            File work = new File(plugin.getDataFolder(), "import/" + arena.name());
            try {
                ArenaImporter.deleteRecursively(work);
                int chunks = ArenaImporter.cut(source, work, arena.x(), arena.z(), radius);
                if (chunks > 0) {
                    copyLevelData(source, work);
                    try {
                        slimeWorlds.importVanilla(work, arena.template(), (int) arena.x(), spawnY, (int) arena.z());
                    } catch (com.infernalsuite.asp.api.exceptions.InvalidWorldException nothingThere) {
                        // ASP rejects worlds whose chunks are all empty.
                        chunks = 0;
                    }
                }
                if (chunks == 0) {
                    slimeWorlds.createEmptyTemplate(arena.template(), (int) arena.x(), spawnY, (int) arena.z());
                }
                sender.sendMessage(I18n.tr("pillars.play.arena.imported", arena.name(), arena.template(),
                        chunks == 0 ? I18n.tr("pillars.play.arena.empty") : I18n.tr("pillars.play.arena.chunks", chunks))
                        .color(NamedTextColor.GREEN));
            } catch (Exception exception) {
                plugin.getLogger().log(java.util.logging.Level.WARNING, "Could not import arena " + arena.name(),
                        exception);
                sender.sendMessage(I18n.tr("pillars.play.arena.import_failed", arena.name(), I18n.message(exception))
                        .color(NamedTextColor.RED));
            } finally {
                try {
                    ArenaImporter.deleteRecursively(work);
                } catch (Exception ignored) {
                    // Replaced by the next import.
                }
            }
        }
    }

    /** Saves the world first if loaded. */
    private File worldFolder(String worldName) {
        World loaded = Bukkit.getWorld(worldName);
        if (loaded != null) {
            loaded.save();
            return loaded.getWorldFolder();
        }
        // 1.21.9+: <level>/dimensions/minecraft/<name>; older: the world container.
        File primary = Bukkit.getWorlds().getFirst().getWorldFolder();
        for (File candidate : List.of(
                new File(primary.getParentFile(), worldName),
                new File(Bukkit.getWorldContainer(), worldName))) {
            if (new File(candidate, "region").isDirectory()) {
                return candidate;
            }
        }
        return null;
    }

    /** ASP needs a level.dat, which 1.21.9+ dimension folders lack; any parent one will do. */
    private static void copyLevelData(File source, File work) throws java.io.IOException {
        File levelData = null;
        for (File folder = source.getAbsoluteFile(); folder != null && levelData == null;
                folder = folder.getParentFile()) {
            File candidate = new File(folder, "level.dat");
            if (candidate.isFile()) {
                levelData = candidate;
            }
        }
        if (levelData == null) {
            throw I18n.exception("pillars.play.error.level_data", source.toString());
        }
        Files.copy(levelData.toPath(), new File(work, "level.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
