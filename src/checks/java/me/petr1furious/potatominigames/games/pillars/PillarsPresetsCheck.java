package me.petr1furious.potatominigames.games.pillars;

import me.petr1furious.potatominigames.core.preset.GamePreset;
import me.petr1furious.potatominigames.core.preset.PresetStore;
import me.petr1furious.potatominigames.games.pillars.PillarsPresets.Parameter;
import me.petr1furious.potatominigames.games.pillars.PillarsPresets.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Run by Gradle check; exercises the real YAML store without a server or test dependency. */
public final class PillarsPresetsCheck {
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final List<String> RESERVED = PillarsConfig.RESERVED;

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("pillars-presets-check-");
        try {
            Path file = directory.resolve("data/pillars-presets.yml");
            var store = PillarsPresets.open(file, RESERVED);
            check(!Files.exists(file), "Opening an empty store must not write data");
            var first =
                    save(
                            store,
                            ALICE,
                            "Alice",
                            "MiX",
                            List.of("adhd", "rave", "tactical", "chaos", "shuffle"));
            check(
                    first.id().equals("mix")
                            && first.name().equals("MiX")
                            && store.find("MIX") == first,
                    "Command IDs normalize case without changing the displayed name");
            check(
                    first.settings()
                                    .modifiers()
                                    .equals(
                                            List.of(
                                                    "chaos",
                                                    "shuffle",
                                                    "tactical",
                                                    "party",
                                                    "adhd"))
                            && first.settings().rave(),
                    "Rave must map to Party with the faster event style");
            for (int i = 1; i < 5; i++) {
                save(store, ALICE, "Alice", "alice" + i, List.of());
            }
            expect(
                    IllegalArgumentException.class,
                    () -> save(store, ALICE, "Alice", "sixth", List.of("chaos")));
            save(store, BOB, "Bob", "bob", List.of("big_house", "party"));
            save(store, ALICE, "Alice", "mix", List.of("tactical"));
            check(
                    store.find("MIX").settings().modifiers().equals(List.of("tactical")),
                    "Overwrite must work at the quota");
            expect(
                    IllegalArgumentException.class,
                    () -> save(store, BOB, "Bob", "mix", List.of("rave")));
            expect(IllegalArgumentException.class, () -> store.delete(BOB, "mix"));
            for (String invalid :
                    List.of(
                            "fusion",
                            "adhd",
                            "rave",
                            "casino_lucky",
                            "../escape",
                            "bad.name",
                            "bad:name",
                            "two words",
                            "_bad",
                            "a".repeat(25))) {
                expect(
                        IllegalArgumentException.class,
                        () -> save(store, BOB, "Bob", invalid, List.of()));
            }
            expect(
                    IllegalArgumentException.class,
                    () -> save(store, BOB, "Bob", "invalid", List.of("party", "rave")));
            expect(
                    IllegalArgumentException.class,
                    () -> save(store, BOB, "Bob", "invalid", List.of("chaos", "chaos")));
            expect(
                    IllegalArgumentException.class,
                    () -> save(store, BOB, "Bob", "invalid", List.of("lucky")));
            store.delete(ALICE, "alice4");
            save(store, ALICE, "Alice", "replacement", List.of("big_house", "adhd"));
            save(store, BOB, "Bob", "rising", List.of("flood", "tactical"));
            var loaded = PillarsPresets.open(file, RESERVED);
            check(
                    loaded.all().equals(store.all()),
                    "Owners, names and modifiers must survive reload");
            check(
                    loaded.find("rising")
                            .settings()
                            .modifiers()
                            .containsAll(List.of("flood", "tactical")),
                    "Flood combines with existing modifiers and survives reload");
            expect(
                    IllegalArgumentException.class,
                    () -> save(loaded, ALICE, "Alice", "sixth", List.of("chaos")));
            expect(
                    UnsupportedOperationException.class,
                    () -> loaded.find("mix").settings().modifiers().add("chaos"));

            String persisted = Files.readString(file);
            Path backup = directory.resolve("backup.yml");
            Files.move(file, backup);
            Files.createDirectory(file);
            expect(IOException.class, () -> save(store, ALICE, "Alice", "mix", List.of("chaos")));
            check(
                    store.find("mix").settings().modifiers().equals(List.of("tactical")),
                    "Failed save must not change memory");
            expect(IOException.class, () -> store.delete(ALICE, "mix"));
            check(store.find("mix") != null, "Failed delete must not change memory");
            check(
                    Files.readString(backup).equals(persisted),
                    "Failed writes must preserve the old file");
            Files.delete(file);
            Files.move(backup, file);
            check(
                    PillarsPresets.open(file, RESERVED).all().equals(store.all()),
                    "Failed writes must preserve reload");

            Path corrupt = directory.resolve("corrupt.yml");
            for (String invalid :
                    List.of(
                            "presets: [",
                            "presets: 42",
                            "unexpected: data",
                            "presets:\n  bad:\n    owner: not-a-uuid\n    modifiers: []\n",
                            "presets:\n  bad:\n    owner: "
                                    + ALICE
                                    + "\n    modifiers: []\n    settings: 42\n",
                            "presets:\n  bad:\n    owner: "
                                    + ALICE
                                    + "\n    modifiers: []\n    settings: {perkChance: 0.2}\n",
                            "presets:\n  bad:\n    owner: "
                                    + ALICE
                                    + "\n"
                                    + "    modifiers: [flood]\n"
                                    + "    settings: {floodRiseSeconds: 0}\n")) {
                Files.writeString(corrupt, invalid);
                expect(IOException.class, () -> PillarsPresets.open(corrupt, RESERVED));
                check(
                        Files.readString(corrupt).equals(invalid),
                        "Invalid files must not be rewritten");
            }
            migrationAndOptions(directory.resolve("legacy.yml"));
            check(
                    PillarsConfig.MODES.containsAll(
                                    List.of("party", "casino", "sniper_duel", "big_house"))
                            && PillarsConfig.MODES.stream()
                                    .noneMatch(PillarsConfig.LEGACY_MODES::contains),
                    "Only the canonical Party and Casino queues are visible; distinct gameplay"
                            + " modes remain");
            check(
                    PillarsConfig.canonicalMode("rave").equals("party")
                            && PillarsConfig.canonicalMode("casino_unlucky").equals("casino")
                            && PillarsConfig.variants("party").equals(List.of("party", "rave"))
                            && PillarsConfig.variants("casino")
                                    .equals(List.of("casino_lucky", "casino", "casino_unlucky"))
                            && PillarsEvents.Casino.fromMode("casino_lucky").goodPercent == 75
                            && PillarsEvents.Casino.fromMode("casino").goodPercent == 50
                            && PillarsEvents.Casino.fromMode("casino_unlucky").goodPercent == 25,
                    "Grouped modes retain their legacy configured variants and event odds");
            System.out.println(
                    "Pillars presets: quota, ownership, migration, options, grouping and atomic"
                            + " failure checks passed");
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void migrationAndOptions(Path file) throws Exception {
        Files.writeString(
                file,
                """
                presets:
                  legacy:
                    owner: %s
                    ownerName: Alice
                    modifiers: [chaos, shuffle, tactical, big_house, rave, adhd, perks, flood]
                """
                        .formatted(ALICE));
        var store = PillarsPresets.open(file, RESERVED);
        var legacy = store.find("legacy");
        check(
                legacy.owner().equals(ALICE)
                        && legacy.ownerName().equals("Alice")
                        && legacy.name().equals("legacy")
                        && legacy.icon().equals(GamePreset.DEFAULT_ICON)
                        && legacy.settings()
                                .legacyModifiers()
                                .equals(
                                        PillarsPresets.COMMAND_MODIFIERS.stream()
                                                .filter(modifier -> !modifier.equals("party"))
                                                .toList())
                        && legacy.settings().overrides().isEmpty(),
                "Old Rave presets retain the owner's full mix and inherit live defaults");

        Settings configured =
                new Settings(
                        legacy.settings().modifiers(),
                        true,
                        Map.of(
                                Parameter.ITEM_INTERVAL,
                                4.0,
                                Parameter.DISTANCE,
                                3.2,
                                Parameter.EVENT_MEAN,
                                23.5,
                                Parameter.ADHD_MEAN,
                                91.2,
                                Parameter.ADHD_DURATION,
                                12.0,
                                Parameter.PERK_CHANCE,
                                0.35,
                                Parameter.FLOOD_DEPTH,
                                7.0,
                                Parameter.FLOOD_RISE,
                                15.0));
        var current = store.save(BOB, "Bob", "MyRave", GamePreset.DEFAULT_ICON, configured);
        var reloaded = PillarsPresets.open(file, RESERVED);
        check(
                reloaded.find("legacy").equals(legacy) && reloaded.find("myrave").equals(current),
                "Rewriting old presets must preserve all modifier and new scalar data");
        check(
                PillarsPresets.fromOptions(configured.options()).equals(configured),
                "Direct-start options must round-trip the same settings as the file");
        check(
                PillarsPresets.fromOptions(Map.of()) == null
                        && PillarsPresets.fromOptions(PillarsPresets.modifiers(List.of()).options())
                                .modifiers()
                                .isEmpty(),
                "An explicitly empty custom match is distinct from random Fusion");
        check(
                configured
                        .select(List.of("tactical"))
                        .overrides()
                        .equals(Map.of(Parameter.ITEM_INTERVAL, 4.0)),
                "Disabling a modifier clears its settings without clearing the independent item"
                        + " interval");
        expect(IllegalArgumentException.class, () -> Parameter.ITEM_INTERVAL.parse("1.5"));
        expect(IllegalArgumentException.class, () -> Parameter.EVENT_MEAN.parse("NaN"));
        expect(IllegalArgumentException.class, () -> Parameter.PERK_CHANCE.parse("Infinity"));
    }

    private static GamePreset<Settings> save(
            PresetStore<Settings> store,
            UUID owner,
            String ownerName,
            String name,
            List<String> modifiers)
            throws IOException {
        return store.save(
                owner,
                ownerName,
                name,
                GamePreset.DEFAULT_ICON,
                PillarsPresets.modifiers(modifiers));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expect(Class<? extends Exception> type, Action action) throws Exception {
        try {
            action.run();
        } catch (Exception exception) {
            if (type.isInstance(exception)) {
                return;
            }
            throw exception;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
