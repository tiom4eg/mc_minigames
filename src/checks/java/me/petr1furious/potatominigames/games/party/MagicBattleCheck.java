package me.petr1furious.potatominigames.games.party;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.i18n.I18n;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/** Imported map provenance/geometry, mana budgets, personal clocks and queue/round integration. */
public final class MagicBattleCheck {
    public static void main(String[] args) throws Exception {
        mapLoader();
        String assetDirectory = System.getenv("POTATOMINIGAMES_CHECK_DATA_DIR");
        if (assetDirectory != null && !assetDirectory.isBlank()) {
            importedMap(Path.of(assetDirectory));
            System.out.println(
                    "Magic Battle: installed Lighthouse geometry and spawn columns verified");
        } else {
            System.out.println(
                    "Magic Battle: fixture loader checked; set POTATOMINIGAMES_CHECK_DATA_DIR"
                            + " to check installed Lighthouse geometry");
        }
        spells();
        mana();
        wandsAndStarters();
        movement();
        integration();
        survival();
        System.out.println(
                "Magic Battle: 42 spell choices, finite mana, independent cooldowns"
                        + " and one-life integration passed");
    }

    private static void mapLoader() throws Exception {
        Path dataFolder = Files.createTempDirectory("magic-map-check-");
        Path file = dataFolder.resolve("maps/magic/lighthouse.bin.gz");
        try {
            try {
                MagicBattleArena.Blueprint.load(dataFolder);
                throw new AssertionError("Missing Lighthouse must fail");
            } catch (CommandFailure failure) {
                check(
                        failure.getMessage().contains(file.toString()),
                        "report where to install Lighthouse");
            }
            Files.createDirectories(file.getParent());
            try (var out =
                    new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
                out.writeBytes("MAGICMAP1");
                out.writeInt(1);
                out.writeUTF("minecraft:oak_planks");
                out.writeInt(1);
                out.writeShort(-2);
                out.writeShort(59);
                out.writeShort(3);
                out.writeShort(0);
                out.writeInt(12);
                for (int i = 0; i < 12; i++) {
                    out.writeFloat(i - 1.5f);
                    out.writeFloat(60);
                    out.writeFloat(3.5f);
                    out.writeFloat(-90);
                }
            }
            var source = MagicBattleArena.Blueprint.load(dataFolder);
            check(
                    source.palette().equals(List.of("minecraft:oak_planks"))
                            && source.blocks()
                                    .equals(
                                            List.of(
                                                    new MagicBattleArena.Cell(
                                                            new MagicBattleArena.Pos(-2, 59, 3),
                                                            0))),
                    "map loader preserves terrain coordinates and palette");
            check(source.spawns().size() == 12, "map loader reads all twelve spawns");
            for (int i = 0; i < 12; i++) {
                check(
                        source.spawns()
                                .get(i)
                                .equals(new MagicBattleArena.Spawn(i - 1.5f, 60, 3.5f, -90)),
                        "map loader preserves spawn positions and yaw");
            }
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(file.getParent());
            Files.deleteIfExists(dataFolder.resolve("maps"));
            Files.deleteIfExists(dataFolder);
        }
    }

    private static void importedMap(Path dataFolder) throws CommandFailure {
        var source = MagicBattleArena.Blueprint.load(dataFolder);
        check(
                source.blocks().size() == 40571 && source.spawns().size() == 12,
                "The installed arena is the full imported Lighthouse asset, not generated islands");
        Map<MagicBattleArena.Pos, String> blocks = new HashMap<>();
        for (var cell : source.blocks()) {
            String data = source.palette().get(cell.palette());
            check(data.startsWith("minecraft:"), "No external custom blocks required");
            String name = data.substring(10).split("\\[")[0];
            check(Material.matchMaterial(name) != null, "Imported Java material exists: " + name);
            check(blocks.put(cell.pos(), data) == null, "Map has unique coordinates");
        }
        var spawns = new HashSet<String>();
        for (var spawn : source.spawns()) {
            int x = (int) Math.floor(spawn.x()), z = (int) Math.floor(spawn.z());
            check(spawns.add(x + ":" + z), "Each player gets a different source island");
            check(
                    blocks.get(new MagicBattleArena.Pos(x, 59, z)).contains("planks"),
                    "Original raised cage markers have a real island surface below");
            check(
                    !blocks.containsKey(new MagicBattleArena.Pos(x, 60, z))
                            && !blocks.containsKey(new MagicBattleArena.Pos(x, 61, z)),
                    "Spawn surface has two clear body blocks");
        }
        check(
                source.palette().stream()
                        .noneMatch(
                                s -> s.contains("command_block") || s.contains("structure_block")),
                "Map conversion contains terrain only, without executable blocks");
    }

    private static void spells() {
        check(
                MagicSpell.values().length == 42 && MagicSpell.School.values().length == 6,
                "Six schools with forty-two distinct spells");
        for (var school : MagicSpell.School.values()) {
            check(
                    school.spells().size() == 7 && new HashSet<>(school.spells()).size() == 7,
                    "Seven spells per school remain available as loot");
        }
        for (var spell : MagicSpell.values()) {
            check(
                    spell.mana > 0 && spell.mana <= MagicMana.MAX && spell.cooldown > 0,
                    "Each spell costs finite mana and has a cooldown");
            for (Locale locale : List.of(Locale.ENGLISH, Locale.forLanguageTag("ru"))) {
                check(
                        !I18n.plain(spell.title(), locale).contains("potatominigames."),
                        "Every spell name renders in both locales: " + spell);
                check(
                        !I18n.plain(spell.description(), locale).contains("potatominigames."),
                        "Every spell description renders in both locales: " + spell);
            }
            if (spell.aimed()) {
                check(
                        spell.range() > 0 && spell.radius() > 0 && spell.warningTicks() > 0,
                        "Aimed spells expose a bounded preview and a dodgeable warning: " + spell);
            }
        }
    }

    private static void wandsAndStarters() {
        check(
                MagicWand.STARTERS.size() == 6
                        && MagicWand.STARTERS.stream().map(s -> s.school).distinct().count() == 6,
                "Starter pool has one basic spell from each school");
        UUID a = new UUID(0, 1), b = new UUID(0, 2), c = new UUID(0, 3);
        MagicStarterChoices choices = new MagicStarterChoices(List.of(a, b, c));
        check(!choices.confirm(a), "An empty selection cannot be confirmed");
        check(
                choices.toggle(a, MagicSpell.FIREBOLT)
                        && choices.toggle(a, MagicSpell.MEND)
                        && !choices.toggle(a, MagicSpell.GALE),
                "No third starter wand can be selected");
        check(
                !choices.toggle(b, MagicSpell.METEOR)
                        && !choices.toggle(new UUID(0, 99), MagicSpell.GALE),
                "Strong spells and unknown players cannot enter the starter selection");
        check(choices.confirm(a) && !choices.ready(), "One confirmation does not start everyone");
        check(
                choices.toggle(b, MagicSpell.RIFT) && choices.confirm(b),
                "A single wand is a valid starter set");
        choices.withdraw(c);
        check(choices.ready(), "A withdrawn player cannot stall the start");
        var selected = choices.finish();
        check(
                selected.get(a).equals(List.of(MagicSpell.FIREBOLT, MagicSpell.MEND))
                        && selected.get(b).equals(List.of(MagicSpell.RIFT))
                        && !selected.containsKey(c),
                "Locking preserves choices and never grants extra wands");
        check(!choices.toggle(a, MagicSpell.FIREBOLT), "Starter choices lock before combat");
        MagicStarterChoices timeout = new MagicStarterChoices(List.of(a));
        check(timeout.finish().get(a).size() == 2, "Timeout grants only two basic fallback wands");
        var normal = java.util.EnumSet.noneOf(MagicSpell.class);
        var dual = java.util.EnumSet.noneOf(MagicSpell.class);
        java.util.Random random = new java.util.Random(1234);
        for (int i = 0; i < 5000; i++) {
            MagicWand wand = MagicWand.loot(random);
            check(wand.cast(false) == wand.first(), "Right-click casts only the primary spell");
            if (wand.enchanted()) {
                check(
                        wand.first() != wand.second()
                                && wand.first().school == wand.second().school,
                        "Enchanted loot pairs distinct spells from one school");
                check(
                        wand.cast(true) == wand.second(),
                        "Left-click casts only the secondary spell");
                dual.add(wand.first());
                dual.add(wand.second());
            } else {
                check(wand.cast(true) == null, "Ordinary wands cannot access a second spell");
                normal.add(wand.first());
            }
        }
        check(
                normal.size() == 42 && dual.size() == 42,
                "All spells remain reachable in ordinary and enchanted loot");
        MagicChestLoot chest = new MagicChestLoot(random, 900);
        check(
                chest.wand(0) != null && chest.wand(1) != null && chest.wool(),
                "Chest starts with two wand offers and blocks");
        chest.takeWand(0);
        chest.takeWool();
        check(
                chest.wand(0) == null && chest.wand(1) != null && !chest.wool(),
                "Taking shared loot removes only that offer for all viewers");
    }

    private static void mana() {
        MagicMana a = new MagicMana(), b = new MagicMana();
        check(
                a.spend(MagicSpell.METEOR, 0) && a.mana() == 50,
                "A powerful spell spends half the pool");
        check(
                !a.spend(MagicSpell.FIREBOLT, 1) && a.mana() == 50,
                "Global cast lock blocks simultaneous casts without spending mana");
        check(
                a.remaining(MagicSpell.FIREBOLT, 1) == 3,
                "Switching spells shows the shared cast lock rather than zero cooldown");
        check(
                a.spend(MagicSpell.FIREBALL, 4) && a.mana() == 18,
                "Another wand can cast after the shared lock");
        check(
                !a.spend(MagicSpell.METEOR, 8),
                "Changing physical wands cannot reset a spell cooldown");
        check(b.ready(MagicSpell.METEOR, 0) && b.mana() == 100, "Pools and cooldowns are personal");
        a.restore(-999);
        check(
                a.mana() == 0 && !a.spend(MagicSpell.STONE_SHOT, 2000),
                "Empty mana blocks offensive casts");
        for (int i = 0; i < 20; i++) a.tick();
        check(Math.abs(a.mana() - 7) < 1e-8, "Passive regeneration is seven mana per second");
        a.restore(1000);
        check(a.mana() == 100, "Chests and sanctuary cannot overfill mana");
        check(a.spend(MagicSpell.METEOR, 240), "Cooldown expires at its exact server tick");
    }

    private static void movement() {
        Player occupant =
                (Player)
                        Proxy.newProxyInstance(
                                Player.class.getClassLoader(),
                                new Class<?>[] {Player.class},
                                (proxy, method, arguments) ->
                                        switch (method.getName()) {
                                            case "getWorld" -> null;
                                            case "getBoundingBox" ->
                                                    new BoundingBox(-.3, 64, -.3, .3, 65.8, .3);
                                            default ->
                                                    throw new AssertionError(
                                                            "Unexpected movement fixture call: "
                                                                    + method.getName());
                                        });
        check(
                MagicCombat.occupied(new Location(null, 0, 65.2, 0), List.of(occupant)),
                "A destination above another player's feet can still overlap their head");
        check(
                !MagicCombat.occupied(new Location(null, 0, 65.8, 0), List.of(occupant)),
                "Body boundaries may touch without intersecting");
        check(
                !MagicCombat.occupied(new Location(null, 0, 64, 0), List.of(occupant), occupant),
                "The caster's old position does not block their own rewind");
    }

    private static void integration() throws Exception {
        var game = new PartyGame((PartyConfig) null);
        check(
                game.modes().contains("magic_battle") && game.maxPlayers("magic_battle") == 12,
                "Party exposes the twelve-player mode");
        check(
                game.createSession(null, "magic_battle") instanceof MagicBattleSession,
                "Standalone queue creates the spell round");
        var legacyMatch =
                new PartyGamesState(
                        List.of(new UUID(0, 1), new UUID(0, 2)),
                        List.of("pvp", "magic_battle", "sumo"),
                        new Random(1));
        check(
                legacyMatch.schedule().size() == 2
                        && legacyMatch.schedule().containsAll(List.of("pvp", "sumo")),
                "Saved pools skip Magic Battle and keep the other rounds");
        var context =
                new PartyRoundContext(
                        null, List.of(), PartyRoundOptions.party(), (reason, result) -> {});
        try {
            PartyRoundFactory.create("magic_battle", null, null, context);
            throw new AssertionError("Party Games can still create a Magic Battle round");
        } catch (IllegalArgumentException expected) {
        }
        try (var stream = MagicBattleCheck.class.getResourceAsStream("/config.yml")) {
            check(stream != null, "Default queue configuration is bundled");
            var queues =
                    YamlConfiguration.loadConfiguration(
                            new InputStreamReader(stream, StandardCharsets.UTF_8));
            check(
                    queues.getBoolean("queues.party.magic_battle.enabled")
                            && queues.getInt("queues.party.magic_battle.minPlayers") == 2
                            && queues.getInt("queues.party.magic_battle.maxPlayers") == 12,
                    "The public magic queue is enabled for two to twelve players");
        }
        try (var stream = MagicBattleCheck.class.getResourceAsStream("/games/party.yml")) {
            check(stream != null, "Default Party configuration is bundled");
            var defaults =
                    YamlConfiguration.loadConfiguration(
                            new InputStreamReader(stream, StandardCharsets.UTF_8));
            check(
                    !List.of(defaults.getString("partyGames.modes").split(","))
                            .contains("magic_battle"),
                    "The default pool excludes Magic Battle");
            check(
                    defaults.getInt("magicBattle.roundSeconds") == 180
                            && defaults.getInt("magicBattle.countdownSeconds") == 5
                            && defaults.getInt("magicBattle.chestRechargeSeconds") == 45,
                    "Bundled duration, countdown and mana-cache defaults match the mode");
        }
    }

    private static void survival() {
        UUID a = new UUID(0, 1), b = new UUID(0, 2), c = new UUID(0, 3);
        var state = new SurvivalStandings(List.of(a, b, c));
        state.eliminate(a, 1);
        check(!state.decided(false, 3), "One death does not restart or end a three-player fight");
        state.eliminate(b, 5);
        check(
                !state.decided(false, 6) && state.decided(false, 7),
                "Victory waits for the simultaneous-death window");
        check(state.result(java.util.Set.of(a, b, c), 7).place(c) == 1, "Last survivor wins");
        state.eliminate(c, 5);
        check(state.survivors().isEmpty(), "All deaths can produce a draw, without a respawn life");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
