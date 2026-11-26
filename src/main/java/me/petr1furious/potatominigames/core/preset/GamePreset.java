package me.petr1furious.potatominigames.core.preset;

import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/** Shared identity for a saved minigame mode; its settings belong to the game. */
public record GamePreset<T>(UUID owner, String ownerName, String name, String icon, T settings) {
    public static final String DEFAULT_ICON = "minecraft:bedrock";
    public static final int NAME_LENGTH = 24;
    public static final int ICON_LENGTH = 1024;
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,23}");

    public GamePreset {
        name = name.strip();
        if (!NAME.matcher(name).matches()) throw I18n.exception("core.preset.invalid_name");
        icon = normalizeIcon(icon);
        if (!icon.equals(DEFAULT_ICON)) parseIcon(icon);
    }

    public String id() {
        return name.toLowerCase(Locale.ROOT);
    }

    public ItemStack item() {
        return parseIcon(icon);
    }

    static String normalizeIcon(String text) {
        String icon = text.strip();
        return icon.isEmpty() ? DEFAULT_ICON : icon;
    }

    /** Accept serialized item SNBT or Minecraft /give syntax with data components. */
    public static ItemStack parseIcon(String text) {
        String spec = normalizeIcon(text);
        if (spec.length() > ICON_LENGTH) throw I18n.exception("core.preset.invalid_icon");
        if (spec.equals(DEFAULT_ICON)) return new ItemStack(Material.BEDROCK);
        try {
            ItemStack item =
                    spec.startsWith("{")
                            ? fromSnbt(spec)
                            : Bukkit.getItemFactory().createItemStack(spec);
            if (!item.isEmpty()) {
                item.setAmount(1);
                return item;
            }
        } catch (IllegalArgumentException | IllegalStateException | IOException ignored) {
            // Keep the invalid input out of chat and preserve the form for correction.
        }
        throw I18n.exception("core.preset.invalid_icon");
    }

    private static ItemStack fromSnbt(String spec) throws IOException {
        CompoundBinaryTag tag = TagStringIO.tagStringIO().asCompound(spec);
        if (!tag.keySet().contains("DataVersion")) {
            tag =
                    CompoundBinaryTag.builder()
                            .put(tag)
                            .putInt("DataVersion", Bukkit.getUnsafe().getDataVersion())
                            .build();
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(tag, bytes, BinaryTagIO.Compression.GZIP);
        return ItemStack.deserializeBytes(bytes.toByteArray());
    }
}
