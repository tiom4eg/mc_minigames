package me.petr1furious.potatominigames.core.preset;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.core.game.Minigame;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Native Minecraft forms, following the upstream settings-screen controls. */
public final class PresetDialog {
    private static final ClickCallback.Options CALLBACK =
            ClickCallback.Options.builder().uses(1).lifetime(Duration.ofMinutes(15)).build();
    private static final int WIDTH = 300;

    private PresetDialog() {}

    @FunctionalInterface
    public interface Action {
        void run(Player player, Form form) throws CommandFailure;
    }

    @FunctionalInterface
    public interface IdentityAction {
        void save(Player player, String name, String icon) throws CommandFailure;
    }

    public record Button(Component label, Action action) {}

    public record Form(DialogResponseView response) {
        public String text(String key) {
            String value = response.getText(key);
            if (value == null) throw I18n.exception("core.preset.invalid_form");
            return value;
        }

        public boolean bool(String key) {
            Boolean value = response.getBoolean(key);
            if (value == null) throw I18n.exception("core.preset.invalid_form");
            return value;
        }

        public float number(String key) {
            Float value = response.getFloat(key);
            if (value == null || !Float.isFinite(value))
                throw I18n.exception("core.preset.invalid_form");
            return value;
        }
    }

    public static void show(
            Player player,
            Component title,
            List<Component> body,
            List<DialogInput> inputs,
            List<Button> buttons,
            Runnable back) {
        show(player, title, body, inputs, buttons, back, null);
    }

    private static void show(
            Player player,
            Component title,
            List<Component> body,
            List<DialogInput> inputs,
            List<Button> buttons,
            Runnable back,
            Component error) {
        render(
                player,
                title,
                body,
                inputs,
                buttons,
                back,
                error,
                next -> show(player, title, body, inputs, buttons, back, next));
    }

    private static void render(
            Player player,
            Component title,
            List<Component> body,
            List<DialogInput> inputs,
            List<Button> buttons,
            Runnable back,
            Component error,
            Consumer<Component> retry) {
        // A response belongs to one player and one screen, even if a different action ID is
        // replayed.
        boolean[] used = {false};
        var actions = buttons.stream().map(button -> action(player, button, used, retry)).toList();
        ActionButton exit =
                action(
                        player,
                        new Button(I18n.tr("core.menu.back"), (viewer, form) -> back.run()),
                        used,
                        retry);
        List<DialogBody> messages = new ArrayList<>();
        if (error != null)
            messages.add(
                    DialogBody.plainMessage(
                            I18n.render(error.colorIfAbsent(NamedTextColor.RED), player), WIDTH));
        for (Component line : body)
            messages.add(DialogBody.plainMessage(I18n.render(line, player), WIDTH));
        DialogBase base =
                DialogBase.builder(I18n.render(title, player))
                        .pause(false)
                        .canCloseWithEscape(true)
                        .afterAction(DialogBase.DialogAfterAction.CLOSE)
                        .body(messages)
                        .inputs(inputs)
                        .build();
        player.showDialog(
                Dialog.create(
                        builder ->
                                builder.empty()
                                        .base(base)
                                        .type(
                                                DialogType.multiAction(actions)
                                                        .columns(2)
                                                        .exitAction(exit)
                                                        .build())));
    }

    private static ActionButton action(
            Player player, Button button, boolean[] used, Consumer<Component> retry) {
        return ActionButton.builder(I18n.render(button.label(), player))
                .width(145)
                .action(
                        DialogAction.customClick(
                                (response, audience) -> {
                                    if (!(audience instanceof Player viewer)
                                            || !viewer.getUniqueId().equals(player.getUniqueId())
                                            || used[0]) return;
                                    used[0] = true;
                                    try {
                                        button.action().run(viewer, new Form(response));
                                    } catch (CommandFailure failure) {
                                        retry.accept(failure.text());
                                    } catch (IllegalArgumentException failure) {
                                        retry.accept(I18n.message(failure));
                                    }
                                },
                                CALLBACK))
                .build();
    }

    public static void identity(
            Player player,
            Component title,
            String name,
            String icon,
            IdentityAction save,
            Runnable back) {
        identity(player, title, name, icon, save, back, null);
    }

    private static void identity(
            Player player,
            Component title,
            String name,
            String icon,
            IdentityAction save,
            Runnable back,
            Component error) {
        String[] draft = {name, icon};
        List<DialogInput> inputs =
                List.of(
                        text(player, "name", "core.preset.name", name, GamePreset.NAME_LENGTH),
                        text(player, "icon", "core.preset.icon", icon, GamePreset.ICON_LENGTH));
        Button button =
                new Button(
                        I18n.tr("core.preset.save"),
                        (viewer, form) -> {
                            draft[0] = form.text("name");
                            draft[1] = form.text("icon");
                            save.save(viewer, draft[0], GamePreset.normalizeIcon(draft[1]));
                        });
        render(
                player,
                title,
                List.of(I18n.tr("core.preset.icon_help")),
                inputs,
                List.of(button),
                back,
                error,
                next ->
                        identity(
                                player,
                                title,
                                bounded(draft[0], GamePreset.NAME_LENGTH),
                                bounded(draft[1], GamePreset.ICON_LENGTH),
                                save,
                                back,
                                next));
    }

    public static DialogInput text(Player player, String id, String key, String initial, int max) {
        return DialogInput.text(id, I18n.render(I18n.tr(key), player))
                .width(WIDTH)
                .initial(initial)
                .maxLength(max)
                .build();
    }

    private static String bounded(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    /** Same free-player roster as a normal direct start, capped at the selected game's limit. */
    public static void start(
            Player player,
            SessionManager sessions,
            Minigame game,
            String mode,
            Map<String, String> options)
            throws CommandFailure {
        List<Player> roster = new ArrayList<>();
        roster.add(player);
        int max = game.maxPlayers(mode);
        for (Player candidate : Bukkit.getOnlinePlayers()) {
            if (max > 0 && roster.size() >= max) break;
            if (!candidate.getUniqueId().equals(player.getUniqueId())
                    && !sessions.isPlaying(candidate)) roster.add(candidate);
        }
        if (roster.size() < game.minPlayers(mode))
            throw new CommandFailure(I18n.tr("core.preset.not_enough", game.minPlayers(mode)));
        sessions.start(game, mode, roster, Map.of(), false, options);
    }
}
