package me.petr1furious.potatominigames.games.party;

import me.petr1furious.potatominigames.core.command.CommandFailure;
import me.petr1furious.potatominigames.core.preset.GamePreset;
import me.petr1furious.potatominigames.core.preset.PresetDialog;
import me.petr1furious.potatominigames.core.session.SessionManager;
import me.petr1furious.potatominigames.i18n.I18n;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class PartyPresetEditor {
    private final PartyGame game;
    private final SessionManager sessions;
    private final Runnable back;
    private final List<String> rounds = new ArrayList<>();
    // An action from an earlier dialog must not address an index in the current order.
    private long view;

    PartyPresetEditor(PartyGame game, SessionManager sessions, Runnable back) {
        this.game = game;
        this.sessions = sessions;
        this.back = back;
    }

    void show(Player player) {
        long current = ++view;
        List<PresetDialog.Button> buttons = new ArrayList<>();
        for (int index = 0; index < rounds.size(); index++) {
            int selected = index;
            buttons.add(button(current, roundLabel(index), p -> editRound(p, selected)));
        }
        if (rounds.size() < PartyGamesState.MAX_CUSTOM_ROUNDS) {
            buttons.add(button(current, text("add"), this::pickRound));
        }
        if (!rounds.isEmpty()) {
            buttons.add(button(current, I18n.tr("core.preset.save"), this::save));
            buttons.add(
                    button(
                            current,
                            I18n.tr("core.preset.start"),
                            p -> {
                                PresetDialog.start(
                                        p,
                                        sessions,
                                        game,
                                        "party_games",
                                        Map.of(
                                                PartyGamesSession.SEQUENCE_OPTION,
                                                String.join(",", rounds)));
                                view++;
                            }));
        }
        PresetDialog.show(
                player,
                text("editor"),
                List.of(
                        text("count", rounds.size(), PartyGamesState.MAX_CUSTOM_ROUNDS),
                        text(rounds.isEmpty() ? "empty" : "edit_hint")),
                List.of(),
                buttons,
                () -> {
                    if (view != current) return;
                    view++;
                    back.run();
                });
    }

    private void pickRound(Player player) {
        long current = ++view;
        List<PresetDialog.Button> buttons = new ArrayList<>(PartyGamesState.MODES.size());
        for (String mode : PartyGamesState.MODES) {
            buttons.add(
                    button(
                            current,
                            PartyRoundFactory.title(mode),
                            p -> {
                                rounds.add(mode);
                                show(p);
                            }));
        }
        PresetDialog.show(
                player, text("pick"), List.of(), List.of(), buttons, backToDraft(player, current));
    }

    private void editRound(Player player, int index) {
        long current = ++view;
        List<PresetDialog.Button> buttons = new ArrayList<>(3);
        if (index > 0) {
            buttons.add(button(current, text("move_up"), p -> move(p, index, index - 1)));
        }
        if (index + 1 < rounds.size()) {
            buttons.add(button(current, text("move_down"), p -> move(p, index, index + 1)));
        }
        buttons.add(
                button(
                        current,
                        text("remove"),
                        p -> {
                            rounds.remove(index);
                            show(p);
                        }));
        PresetDialog.show(
                player,
                roundLabel(index),
                List.of(),
                List.of(),
                buttons,
                backToDraft(player, current));
    }

    private void move(Player player, int from, int to) {
        Collections.swap(rounds, from, to);
        show(player);
    }

    private void save(Player player) {
        long current = ++view;
        PresetDialog.identity(
                player,
                text("save_title"),
                "",
                GamePreset.DEFAULT_ICON,
                (p, name, icon) -> {
                    if (view != current) return;
                    game.savePreset(p, name, icon, rounds);
                    show(p);
                },
                backToDraft(player, current));
    }

    private Runnable backToDraft(Player player, long current) {
        return () -> {
            if (view == current) show(player);
        };
    }

    private PresetDialog.Button button(long current, Component label, Step action) {
        return new PresetDialog.Button(
                label,
                (player, form) -> {
                    if (view == current) action.run(player);
                });
    }

    private Component roundLabel(int index) {
        return text("round", index + 1, PartyRoundFactory.title(rounds.get(index)));
    }

    private static Component text(String key, Object... arguments) {
        return I18n.tr("party.rounds.preset." + key, arguments);
    }

    @FunctionalInterface
    private interface Step {
        void run(Player player) throws CommandFailure;
    }
}
