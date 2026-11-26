package me.petr1furious.potatominigames.core.preset;

import me.petr1furious.potatominigames.i18n.I18n;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** One game-local library, with case-insensitive unique names and a per-owner quota. */
public final class PresetStore<T> {
    public static final int LIMIT = 5;

    public interface Codec<T> {
        T read(ConfigurationSection entry);

        void write(ConfigurationSection entry, T settings);
    }

    private final Path file;
    private final List<String> reserved;
    private final Codec<T> codec;
    private final Map<String, GamePreset<T>> presets = new LinkedHashMap<>();

    public PresetStore(Path file, List<String> reserved, Codec<T> codec) throws IOException {
        this.file = file.toAbsolutePath();
        this.reserved = List.copyOf(reserved);
        this.codec = codec;
        if (!Files.exists(this.file)) return;
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(this.file.toFile());
            ConfigurationSection list = yaml.getConfigurationSection("presets");
            if (list == null) {
                if (!yaml.getKeys(false).isEmpty())
                    throw new IllegalArgumentException("Expected presets section");
                return;
            }
            for (String id : list.getKeys(false)) {
                ConfigurationSection entry = list.getConfigurationSection(id);
                if (entry == null) throw new IllegalArgumentException("Invalid preset: " + id);
                UUID owner = UUID.fromString(entry.getString("owner", ""));
                var preset =
                        new GamePreset<>(
                                owner,
                                entry.getString("ownerName", owner.toString()),
                                entry.getString("name", id),
                                entry.getString("icon", GamePreset.DEFAULT_ICON),
                                codec.read(entry));
                if (!preset.id().equals(id) || count(owner) >= LIMIT)
                    throw new IllegalArgumentException("Invalid preset ID or owner quota: " + id);
                requireUnreserved(id);
                presets.put(id, preset);
            }
        } catch (InvalidConfigurationException | IllegalArgumentException exception) {
            throw new IOException(
                    "Invalid presets in " + file + ": " + exception.getMessage(), exception);
        }
    }

    public List<GamePreset<T>> all() {
        return presets.values().stream().sorted(Comparator.comparing(GamePreset::id)).toList();
    }

    public List<String> names() {
        return presets.keySet().stream().sorted().toList();
    }

    public GamePreset<T> find(String name) {
        return presets.get(name.toLowerCase(Locale.ROOT));
    }

    public GamePreset<T> save(UUID owner, String ownerName, String name, String icon, T settings)
            throws IOException {
        var preset = new GamePreset<>(owner, ownerName, name, icon, settings);
        String id = preset.id();
        requireUnreserved(id);
        GamePreset<T> previous = presets.get(id);
        if (previous != null && !previous.owner().equals(owner))
            throw I18n.exception("core.preset.owner", previous.ownerName());
        if (previous == null && count(owner) >= LIMIT)
            throw I18n.exception("core.preset.limit", LIMIT);
        persist(id, preset);
        presets.put(id, preset);
        return preset;
    }

    public void delete(UUID owner, String name) throws IOException {
        GamePreset<T> preset = find(name.strip());
        if (preset == null) throw I18n.exception("core.preset.unknown", name);
        if (!preset.owner().equals(owner))
            throw I18n.exception("core.preset.owner", preset.ownerName());
        persist(preset.id(), null);
        presets.remove(preset.id());
    }

    private void requireUnreserved(String id) {
        if (reserved.contains(id)) throw I18n.exception("core.preset.reserved", id);
    }

    private long count(UUID owner) {
        return presets.values().stream().filter(preset -> preset.owner().equals(owner)).count();
    }

    private void persist(String id, GamePreset<T> replacement) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection list = yaml.createSection("presets");
        for (GamePreset<T> preset : presets.values()) {
            if (!preset.id().equals(id)) write(list, preset);
        }
        if (replacement != null) write(list, replacement);
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "presets-", ".tmp");
        try {
            Files.writeString(temporary, yaml.saveToString(), StandardCharsets.UTF_8);
            Files.move(
                    temporary,
                    file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void write(ConfigurationSection list, GamePreset<T> preset) {
        ConfigurationSection entry = list.createSection(preset.id());
        entry.set("owner", preset.owner().toString());
        entry.set("ownerName", preset.ownerName());
        entry.set("name", preset.name());
        entry.set("icon", preset.icon());
        codec.write(entry, preset.settings());
    }
}
