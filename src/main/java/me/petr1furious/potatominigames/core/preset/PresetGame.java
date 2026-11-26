package me.petr1furious.potatominigames.core.preset;

import java.util.List;

import org.bukkit.entity.Player;

import me.petr1furious.potatominigames.core.game.Minigame;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.core.command.CommandFailure;

public interface PresetGame extends Minigame {
    List<? extends GamePreset<?>> presets();

    void openPresetEditor(Player player, SessionManager sessions, Runnable back);

    void deletePreset(Player player, String id) throws CommandFailure;

    default List<String> classicModes() {
        var custom = presets().stream().map(GamePreset::id).toList();
        return modes().stream().filter(mode -> !custom.contains(mode)).toList();
    }
}
