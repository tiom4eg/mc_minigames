package me.petr1furious.potatominigames.games.party;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.core.config.ConfigRegistry;
import me.petr1furious.potatominigames.core.game.Ballot;
import me.petr1furious.potatominigames.core.game.GameSession;
import me.petr1furious.potatominigames.core.game.SessionServices;
import me.petr1furious.potatominigames.core.preset.GamePreset;
import me.petr1furious.potatominigames.core.preset.PresetGame;
import me.petr1furious.potatominigames.core.preset.PresetStore;
import me.petr1furious.potatominigames.core.queue.QueueService;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class PartyGame implements PresetGame {
    private static final List<String> CLASSIC_MODES =
            List.of(
                    "pvp",
                    "sumo",
                    "pvp_ffa",
                    "pvp_select",
                    "sumo_ffa",
                    "arms_race",
                    "one_shot",
                    "tnt_run",
                    "mob_count",
                    "rhino",
                    "miners",
                    "magic_battle",
                    "color_floor",
                    "anvils",
                    "build_memory",
                    "gartic_phone",
                    FurryQuestSession.ID,
                    "tool_race",
                    "parkour_race",
                    "hole_in_the_wall",
                    "hot_potato",
                    "jump_rope",
                    "drop_tower",
                    "chicken_shooter",
                    "less_is_better",
                    "wait_for_it",
                    "party_games");

    private final PartyConfig config;
    private final ConfigRegistry configs;
    private final JavaPlugin plugin;
    private final PresetStore<List<String>> presets;
    private List<String> modeIds = CLASSIC_MODES;
    private QueueService queues;

    public PartyGame(ConfigRegistry configs) {
        this(new PartyConfig(configs));
    }

    PartyGame(PartyConfig config) {
        this(config, null, null, null);
    }

    public PartyGame(JavaPlugin plugin, ConfigRegistry configs) {
        this(new PartyConfig(configs), configs, plugin, loadPresets(plugin));
        refreshPresetModes();
    }

    private PartyGame(
            PartyConfig config,
            ConfigRegistry configs,
            JavaPlugin plugin,
            PresetStore<List<String>> presets) {
        this.config = config;
        this.configs = configs;
        this.plugin = plugin;
        this.presets = presets;
    }

    private static PresetStore<List<String>> loadPresets(JavaPlugin plugin) {
        try {
            return new PresetStore<>(
                    plugin.getDataFolder().toPath().resolve("data/party-presets.yml"),
                    CLASSIC_MODES,
                    new PresetStore.Codec<List<String>>() {
                        @Override
                        public List<String> read(ConfigurationSection entry) {
                            return PartyGamesState.checkedSequence(entry.getList("rounds"));
                        }

                        @Override
                        public void write(ConfigurationSection entry, List<String> rounds) {
                            entry.set("rounds", rounds);
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot load Party Games presets; existing data has been left untouched.",
                    exception);
        }
    }

    public void setQueues(QueueService queues) {
        this.queues = queues;
    }

    private void refreshPresetModes() {
        var modes = new ArrayList<>(CLASSIC_MODES);
        modes.addAll(presets.names());
        modeIds = List.copyOf(modes);
        if (queues != null) queues.refreshQueues(this, configs);
    }

    @Override
    public List<GamePreset<List<String>>> presets() {
        return presets == null ? List.of() : presets.all();
    }

    private GamePreset<List<String>> preset(String mode) {
        return presets == null ? null : presets.find(mode);
    }

    @Override
    public void openPresetEditor(Player player, SessionManager sessions, Runnable back) {
        new PartyPresetEditor(this, sessions, back).show(player);
    }

    private void requirePresetIdle(String name) throws CommandFailure {
        var preset = preset(name.strip());
        if (preset != null && queues != null && queues.isModeInUse(this, preset.id()))
            throw new CommandFailure(I18n.tr("party.rounds.preset.error.busy"));
    }

    void savePreset(Player player, String name, String icon, List<String> rounds)
            throws CommandFailure {
        requirePresetIdle(name);
        try {
            var saved =
                    presets.save(
                            player.getUniqueId(),
                            player.getName(),
                            name,
                            icon,
                            PartyGamesState.checkedSequence(rounds));
            refreshPresetModes();
            player.sendMessage(
                    I18n.render(
                            I18n.tr("party.rounds.preset.saved", saved.name())
                                    .color(NamedTextColor.GREEN),
                            player));
        } catch (IllegalArgumentException exception) {
            throw new CommandFailure(I18n.message(exception));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not save Party Games preset", exception);
            throw new CommandFailure(I18n.tr("party.rounds.preset.error.save"));
        }
    }

    @Override
    public void deletePreset(Player player, String id) throws CommandFailure {
        requirePresetIdle(id);
        try {
            presets.delete(player.getUniqueId(), id);
            refreshPresetModes();
        } catch (IllegalArgumentException exception) {
            throw new CommandFailure(I18n.message(exception));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not delete Party Games preset", exception);
            throw new CommandFailure(I18n.tr("party.rounds.preset.error.delete"));
        }
    }

    @Override
    public String id() {
        return "party";
    }

    @Override
    public Component displayName() {
        return I18n.tr("party.rounds.name").color(NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public List<String> modes() {
        return modeIds;
    }

    @Override
    public List<String> classicModes() {
        return CLASSIC_MODES;
    }

    @Override
    public boolean showModes() {
        return true;
    }

    @Override
    public List<ModeGroup> modeGroups() {
        return List.of(
                new ModeGroup(
                        I18n.tr("party.rounds.mode.pvp"),
                        Material.IRON_SWORD,
                        List.of("pvp", "pvp_ffa", "pvp_select")));
    }

    @Override
    public Component groupedModeTitle(String mode) {
        return List.of("pvp", "pvp_ffa", "pvp_select").contains(mode)
                ? I18n.tr("party.rounds.pvp_variant." + mode)
                : modeTitle(mode);
    }

    @Override
    public List<Ballot> ballots(String mode) {
        if (!mode.equals("party_games")) return List.of();
        return List.of(
                new Ballot(
                        "length",
                        "match length",
                        () -> List.of("small", "medium", "large", "marathon"),
                        () -> "medium",
                        List.of(
                                Material.LIME_DYE,
                                Material.YELLOW_DYE,
                                Material.ORANGE_DYE,
                                Material.NETHER_STAR)));
    }

    @Override
    public Material icon() {
        return Material.FIREWORK_ROCKET;
    }

    @Override
    public Component modeTitle(String mode) {
        var preset = preset(mode);
        if (preset != null) return Component.text(preset.name());
        return mode.equals("pvp_ffa") || mode.equals("pvp_select") || mode.equals("sumo_ffa")
                ? I18n.tr("party.rounds.mode." + mode)
                : mode.equals("party_games")
                        ? I18n.tr("party.rounds.mode.party_games")
                        : PartyRoundFactory.title(mode);
    }

    @Override
    public Material icon(String mode) {
        return switch (mode) {
            case "pvp" -> Material.IRON_SWORD;
            case "sumo" -> Material.PISTON;
            case "arms_race" -> Material.CROSSBOW;
            case "tnt_run" -> Material.TNT;
            case "mob_count" -> Material.PIG_SPAWN_EGG;
            case "rhino" -> Material.RAVAGER_SPAWN_EGG;
            case "one_shot" -> Material.BOW;
            case "parkour_race" -> Material.CLOCK;
            case "magic_battle" -> Material.BLAZE_ROD;
            case "miners" -> Material.DIAMOND_PICKAXE;
            case "tool_race" -> Material.IRON_PICKAXE;
            case "color_floor" -> Material.LIME_CONCRETE;
            case "pvp_ffa" -> Material.DIAMOND_SWORD;
            case "pvp_select" -> Material.NETHER_STAR;
            case "sumo_ffa" -> Material.STICK;
            case "anvils" -> Material.ANVIL;
            case "hole_in_the_wall" -> Material.SLIME_BLOCK;
            case "party_games" -> Material.FIREWORK_ROCKET;
            case "build_memory" -> Material.BRICKS;
            case "gartic_phone" -> Material.WRITABLE_BOOK;
            case "hot_potato" -> Material.BAKED_POTATO;
            case "jump_rope" -> Material.LEAD;
            case "drop_tower" -> Material.SLIME_BLOCK;
            case "chicken_shooter" -> Material.CHICKEN_SPAWN_EGG;
            case "less_is_better" -> Material.RED_DYE;
            case "wait_for_it" -> Material.CRAFTING_TABLE;
            case FurryQuestSession.ID -> Material.SWEET_BERRIES;
            default -> icon();
        };
    }

    @Override
    public int maxPlayers(String mode) {
        if (preset(mode) != null) return 12;
        return mode.equals("pvp")
                        || mode.equals("sumo")
                        || mode.equals("miners")
                        || mode.equals("magic_battle")
                        || PartyGamesState.QUICK_MODES.contains(mode)
                        || List.of(
                                        "color_floor",
                                        "anvils",
                                        "build_memory",
                                        "gartic_phone",
                                        FurryQuestSession.ID,
                                        "tool_race",
                                        "parkour_race",
                                        "hole_in_the_wall",
                                        "party_games")
                                .contains(mode)
                ? 12
                : 16;
    }

    @Override
    public Component description(String mode) {
        var preset = preset(mode);
        if (preset != null)
            return I18n.tr(
                    "party.rounds.preset.description",
                    preset.ownerName(),
                    preset.settings().size());
        return I18n.tr("party.rounds.description." + (CLASSIC_MODES.contains(mode) ? mode : "pvp"));
    }

    @Override
    public GameSession createSession(SessionServices services, String mode) {
        var preset = preset(mode);
        if (preset != null) return new PartyGamesSession(services, config, preset.settings());
        if (PartyGamesState.QUICK_MODES.contains(mode))
            return PartyRoundFactory.create(
                    mode, services, config, PartyRoundContext.standalone(services));
        if (mode.equals("pvp_select")) return new DuelSession(services, config, false, true, true);
        if (mode.equals("gartic_phone")) return new GarticPhoneSession(services, config);
        if (mode.equals("one_shot"))
            return new OneShotSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("parkour_race"))
            return new ParkourRaceSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("hole_in_the_wall"))
            return new HoleWallSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("party_games")) return new PartyGamesSession(services, config);
        if (mode.equals("color_floor"))
            return new ColorFloorSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("anvils"))
            return new AnvilSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("build_memory"))
            return new BuildMemorySession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals(FurryQuestSession.ID))
            return new FurryQuestSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("tool_race"))
            return new ToolRaceSession(services, config, PartyRoundContext.standalone(services));
        if (mode.equals("magic_battle")) return new MagicBattleSession(services, config);
        if (mode.equals("miners")) return new MinersSession(services, config);
        if (mode.equals("arms_race")) return new ArmsRaceSession(services, config);
        if (mode.equals("tnt_run")) return new TntRunSession(services, config);
        if (mode.equals("mob_count")) return new MobCountSession(services, config);
        if (mode.equals("rhino")) return new RhinoSession(services, config);
        return new DuelSession(
                services,
                config,
                mode.equals("sumo") || mode.equals("sumo_ffa"),
                mode.equals("pvp_ffa") || mode.equals("sumo_ffa"));
    }
}
