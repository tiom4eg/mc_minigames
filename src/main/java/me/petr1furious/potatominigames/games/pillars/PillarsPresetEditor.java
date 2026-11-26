package me.petr1furious.potatominigames.games.pillars;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.core.preset.GamePreset;
import me.petr1furious.potatominigames.core.preset.PresetDialog;
import me.petr1furious.potatominigames.core.preset.PresetDialog.Button;
import me.petr1furious.potatominigames.core.preset.PresetDialog.Form;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.games.pillars.PillarsPresets.Parameter;
import me.petr1furious.potatominigames.games.pillars.PillarsPresets.Settings;
import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class PillarsPresetEditor {
    private static final String FUSION = "fusion";
    private static final Map<String, List<Parameter>> MODIFIER_FIELDS =
            Parameter.ALL.stream()
                    .filter(parameter -> parameter.modifier != null)
                    .collect(
                            Collectors.groupingBy(
                                    parameter -> parameter.modifier,
                                    Collectors.toUnmodifiableList()));

    private final PillarsGame game;
    private final PillarsConfig config;
    private final SessionManager sessions;
    private final Runnable back;

    PillarsPresetEditor(
            PillarsGame game, PillarsConfig config, SessionManager sessions, Runnable back) {
        this.game = game;
        this.config = config;
        this.sessions = sessions;
        this.back = back;
    }

    void open(Player player) {
        show(player, PillarsPresets.modifiers(List.of()));
    }

    private void show(Player player, Settings settings) {
        List<DialogInput> inputs = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        buttons.add(
                new Button(
                        I18n.tr("pillars.play.editor.start"),
                        (viewer, form) -> {
                            Settings selected = selected(settings, form);
                            try {
                                PresetDialog.start(
                                        viewer, sessions, game, FUSION, selected.options());
                            } catch (CommandFailure failure) {
                                viewer.sendMessage(
                                        failure.text().colorIfAbsent(NamedTextColor.RED));
                                show(viewer, selected);
                            }
                        }));
        buttons.add(
                new Button(
                        I18n.tr("core.preset.save"),
                        (viewer, form) -> {
                            Settings selected = selected(settings, form);
                            PresetDialog.identity(
                                    viewer,
                                    I18n.tr("pillars.play.editor.save_title"),
                                    "",
                                    GamePreset.DEFAULT_ICON,
                                    (owner, name, icon) -> {
                                        game.savePreset(owner, name, icon, selected);
                                        back.run();
                                    },
                                    () -> show(viewer, selected));
                        }));
        buttons.add(
                new Button(
                        Parameter.ITEM_INTERVAL.label(),
                        (viewer, form) ->
                                parameters(
                                        viewer,
                                        selected(settings, form),
                                        null,
                                        List.of(Parameter.ITEM_INTERVAL))));
        for (String modifier : PillarsPresets.MODIFIERS) {
            Component name = modifierName(modifier);
            inputs.add(
                    DialogInput.bool(modifier, I18n.render(name, player))
                            .initial(settings.enabled(modifier))
                            .build());
            List<Parameter> fields = MODIFIER_FIELDS.getOrDefault(modifier, List.of());
            if (!fields.isEmpty()) {
                buttons.add(
                        new Button(
                                I18n.tr("pillars.play.editor.configure", name),
                                (viewer, form) -> {
                                    Settings selected = selected(settings, form);
                                    if (selected.enabled(modifier)) {
                                        parameters(viewer, selected, modifier, fields);
                                    } else {
                                        viewer.sendMessage(
                                                I18n.tr("pillars.play.editor.enable_first", name)
                                                        .color(NamedTextColor.RED));
                                        show(viewer, selected);
                                    }
                                }));
            }
        }
        PresetDialog.show(
                player,
                I18n.tr("pillars.play.editor.title"),
                List.of(
                        I18n.tr("pillars.play.editor.help"),
                        I18n.tr("pillars.play.editor.item_help")),
                inputs,
                buttons,
                back);
    }

    private void parameters(
            Player player, Settings settings, String modifier, List<Parameter> fields) {
        List<DialogInput> inputs = new ArrayList<>();
        boolean party = "party".equals(modifier);
        if (party) {
            inputs.add(
                    DialogInput.singleOption(
                                    "style",
                                    I18n.render(I18n.tr("pillars.play.editor.event_style"), player),
                                    PillarsConfig.PARTY_VARIANTS.stream()
                                            .map(
                                                    variant ->
                                                            OptionEntry.create(
                                                                    variant,
                                                                    I18n.render(
                                                                            game.variantTitle(
                                                                                    variant),
                                                                            player),
                                                                    variant.equals(
                                                                            settings
                                                                                    .partyVariant())))
                                            .toList())
                            .width(300)
                            .build());
        }
        PillarsConfig.Mode fusion = config.mode(FUSION);
        for (Parameter parameter : fields) {
            Double override = settings.overrides().get(parameter);
            String fallback = Parameter.format(config.defaultValue(fusion, settings, parameter));
            Component label =
                    I18n.tr("pillars.play.editor.default_value", parameter.label(), fallback);
            inputs.add(
                    DialogInput.text(parameter.key, I18n.render(label, player))
                            .width(300)
                            .maxLength(24)
                            .initial(override == null ? "" : Parameter.format(override))
                            .build());
        }
        Component title =
                modifier == null ? Parameter.ITEM_INTERVAL.label() : modifierName(modifier);
        Button apply =
                new Button(
                        I18n.tr("pillars.play.editor.apply"),
                        (viewer, form) -> {
                            boolean rave = settings.rave();
                            if (party) {
                                String variant = form.text("style");
                                if (!PillarsConfig.PARTY_VARIANTS.contains(variant))
                                    throw I18n.exception("core.preset.invalid_form");
                                rave = variant.equals("rave");
                            }
                            var values = new EnumMap<Parameter, Double>(Parameter.class);
                            values.putAll(settings.overrides());
                            for (Parameter parameter : fields) {
                                String raw = form.text(parameter.key);
                                if (raw.isBlank()) values.remove(parameter);
                                else values.put(parameter, parameter.parse(raw));
                            }
                            show(viewer, new Settings(settings.modifiers(), rave, values));
                        });
        PresetDialog.show(
                player,
                title,
                List.of(I18n.tr("pillars.play.editor.default_help")),
                inputs,
                List.of(apply),
                () -> show(player, settings));
    }

    private static Settings selected(Settings settings, Form form) {
        return settings.select(PillarsPresets.MODIFIERS.stream().filter(form::bool).toList());
    }

    private Component modifierName(String modifier) {
        return game.modeTitle(modifier.equals("shuffle") ? "item-shuffle" : modifier);
    }
}
