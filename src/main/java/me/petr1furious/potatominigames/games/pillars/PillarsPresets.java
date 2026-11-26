package me.petr1furious.potatominigames.games.pillars;

import me.petr1furious.potatominigames.core.preset.PresetStore;
import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.text.Component;

import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The settings shared by saved, dialog-launched and randomly rolled Fusion matches. */
final class PillarsPresets {
    static final List<String> MODIFIERS =
            List.of("chaos", "shuffle", "tactical", "big_house", "party", "adhd", "perks", "flood");
    static final List<String> COMMAND_MODIFIERS =
            List.of(
                    "chaos",
                    "shuffle",
                    "tactical",
                    "big_house",
                    "party",
                    "rave",
                    "adhd",
                    "perks",
                    "flood");
    private static final String MODIFIERS_OPTION = "pillars.fusion";
    private static final String OPTION_PREFIX = "pillars.";

    enum Parameter {
        ITEM_INTERVAL(null, "itemInterval", 1, 300, true),
        DISTANCE("big_house", "distanceMultiplier", 0.25, 10, false),
        EVENT_MEAN("party", "eventMeanSeconds", 1, 3600, false),
        ADHD_MEAN("adhd", "adhdMeanSeconds", 1, 3600, false),
        ADHD_DURATION("adhd", "adhdDurationSeconds", 1, 300, true),
        PERK_CHANCE("perks", "perkChance", 0, 1, false),
        FLOOD_DEPTH("flood", "floodStartBelow", 1, 64, true),
        FLOOD_RISE("flood", "floodRiseSeconds", 1, 300, true);

        static final List<Parameter> ALL = List.of(values());
        private static final Set<String> KEYS =
                ALL.stream()
                        .map(parameter -> parameter.key)
                        .collect(Collectors.toUnmodifiableSet());

        final String modifier;
        final String key;
        private final double min;
        private final double max;
        private final boolean whole;

        Parameter(String modifier, String key, double min, double max, boolean whole) {
            this.modifier = modifier;
            this.key = key;
            this.min = min;
            this.max = max;
            this.whole = whole;
        }

        Component label() {
            return I18n.tr("pillars.play.editor.parameter." + key);
        }

        double parse(String raw) {
            try {
                double value = Double.parseDouble(raw.trim());
                if (value >= min && value <= max && (!whole || value == Math.rint(value))) {
                    return value == 0 ? 0 : value;
                }
            } catch (NumberFormatException ignored) {
                // Report the same range for non-numeric and out-of-range values.
            }
            throw I18n.exception(
                    whole ? "pillars.play.editor.whole_range" : "pillars.play.editor.range",
                    label(),
                    format(min),
                    format(max));
        }

        static String format(double value) {
            return value == (long) value ? Long.toString((long) value) : Double.toString(value);
        }
    }

    record Settings(List<String> modifiers, boolean rave, Map<Parameter, Double> overrides) {
        Settings {
            modifiers = List.copyOf(modifiers);
            overrides = Map.copyOf(overrides);
        }

        boolean enabled(String modifier) {
            return modifier == null || modifiers.contains(modifier);
        }

        String partyVariant() {
            return rave ? "rave" : "party";
        }

        List<String> legacyModifiers() {
            return rave
                    ? modifiers.stream().map(id -> id.equals("party") ? "rave" : id).toList()
                    : modifiers;
        }

        Settings select(List<String> selected) {
            if (modifiers.equals(selected)) return this;
            var retained = new EnumMap<Parameter, Double>(Parameter.class);
            overrides.forEach(
                    (parameter, value) -> {
                        if (parameter.modifier == null || selected.contains(parameter.modifier))
                            retained.put(parameter, value);
                    });
            return new Settings(selected, rave && selected.contains("party"), retained);
        }

        Map<String, String> options() {
            var options = new LinkedHashMap<String, String>();
            options.put(MODIFIERS_OPTION, String.join(",", legacyModifiers()));
            overrides.forEach(
                    (parameter, value) ->
                            options.put(OPTION_PREFIX + parameter.key, Parameter.format(value)));
            return Map.copyOf(options);
        }
    }

    static Settings modifiers(List<String> raw) {
        var selected = new LinkedHashSet<String>();
        for (String value : raw) {
            String modifier = value.toLowerCase(Locale.ROOT);
            if (!COMMAND_MODIFIERS.contains(modifier)) {
                throw I18n.exception(
                        "pillars.play.preset_error.unknown_modifier",
                        value,
                        String.join(", ", COMMAND_MODIFIERS));
            }
            if (!selected.add(modifier)) {
                throw I18n.exception("pillars.play.preset_error.duplicate_modifier", modifier);
            }
        }
        boolean rave = selected.remove("rave");
        if (rave && !selected.add("party")) {
            throw I18n.exception("pillars.play.preset_error.party_rave");
        }
        return new Settings(MODIFIERS.stream().filter(selected::contains).toList(), rave, Map.of());
    }

    /** Null means the built-in random Fusion, not a deliberately empty modifier selection. */
    static Settings fromOptions(Map<String, String> options) {
        String raw = options.get(MODIFIERS_OPTION);
        if (raw == null) return null;
        return parseValues(
                modifiers(raw.isEmpty() ? List.of() : List.of(raw.split(",", -1))),
                parameter -> options.get(OPTION_PREFIX + parameter.key));
    }

    static PresetStore<Settings> open(Path file, List<String> reserved) throws IOException {
        return new PresetStore<>(
                file,
                reserved,
                new PresetStore.Codec<Settings>() {
                    @Override
                    public Settings read(ConfigurationSection entry) {
                        List<?> raw = entry.getList("modifiers");
                        if (raw == null
                                || raw.stream().anyMatch(value -> !(value instanceof String))) {
                            throw I18n.exception(
                                    "pillars.play.preset_error.modifiers", entry.getName());
                        }
                        ConfigurationSection scalars = entry.getConfigurationSection("settings");
                        if (entry.contains("settings")
                                && (scalars == null
                                        || !Parameter.KEYS.containsAll(scalars.getKeys(false)))) {
                            throw I18n.exception(
                                    "pillars.play.editor.invalid_settings", entry.getName());
                        }
                        return parseValues(
                                modifiers(raw.stream().map(String.class::cast).toList()),
                                parameter -> {
                                    Object value =
                                            scalars == null ? null : scalars.get(parameter.key);
                                    return value == null ? null : value.toString();
                                });
                    }

                    @Override
                    public void write(ConfigurationSection entry, Settings settings) {
                        entry.set("modifiers", settings.legacyModifiers());
                        settings.overrides.forEach(
                                (parameter, value) ->
                                        entry.set("settings." + parameter.key, value));
                    }
                });
    }

    private static Settings parseValues(Settings settings, Function<Parameter, String> get) {
        var values = new EnumMap<Parameter, Double>(Parameter.class);
        for (Parameter parameter : Parameter.ALL) {
            String raw = get.apply(parameter);
            if (raw == null) continue;
            if (!settings.enabled(parameter.modifier)) {
                throw I18n.exception("pillars.play.editor.disabled_setting", parameter.label());
            }
            values.put(parameter, parameter.parse(raw));
        }
        return new Settings(settings.modifiers(), settings.rave(), values);
    }
}
