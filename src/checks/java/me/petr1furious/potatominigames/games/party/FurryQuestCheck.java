package me.petr1furious.potatominigames.games.party;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class FurryQuestCheck {
    private static final List<UUID> PLAYERS =
            List.of(new UUID(0, 1), new UUID(0, 2), new UUID(0, 3));

    private static void require(boolean condition, String detail) {
        if (!condition) throw new AssertionError(detail);
    }

    private static FurryQuest round() {
        return new FurryQuest(PLAYERS, new Random(42));
    }

    private static Set<FurryQuest.Site> finishRoute(FurryQuest game, UUID id, boolean clockwise) {
        var seen = EnumSet.noneOf(FurryQuest.Site.class);
        var assigned = game.assignments(id);
        if (!assigned.contains(FurryQuest.Site.CONTACT))
            require(
                    game.contactSecondsLeft(id) == 0 && !game.contactTick(id, 0),
                    "Contact requires an assignment");
        for (var site : assigned.reversed()) {
            require(game.unfinished(id, site), "Each assigned task is available from the start");
            require(seen.add(site), "Each landmark appears once");
            require(!game.act(id, site, 20, 20, true, false, false), "Outside the zone");
            boolean done =
                    switch (site) {
                        case CONTACT -> {
                            require(
                                    !game.contactTick(game.sniper(), 0),
                                    "The hunter cannot make contact");
                            for (int i = 0; i < 299; i++)
                                require(
                                        !game.contactTick(id, 16),
                                        "Waiting at the edge of the contact radius");
                            require(
                                    game.contactSecondsLeft(id) == 1,
                                    "One second left on the guide");
                            for (int i = 0; i < 99; i++)
                                require(
                                        !game.contactTick(id, 16.01)
                                                && game.contactSecondsLeft(id) == 1,
                                        "Progress pauses during the grace period");
                            require(
                                    !game.contactTick(id, 16.01)
                                            && game.contactSecondsLeft(id) == 15,
                                    "Five seconds continuously outside resets progress");
                            for (int i = 0; i < 299; i++)
                                require(
                                        !game.contactTick(id, 0),
                                        "A fresh fifteen seconds are required");
                            game.contactTick(id, 17);
                            boolean contacted = game.contactTick(id, 0);
                            require(!game.contactTick(id, 0), "Contact scores only once");
                            yield contacted;
                        }
                        case POND -> {
                            for (int i = 0; i < 39; i++)
                                require(
                                        !game.act(id, site, 2, 0, true, true, false),
                                        "Drink takes two seconds");
                            game.act(id, site, 2, 0, false, true, false);
                            for (int i = 0; i < 39; i++)
                                require(
                                        !game.act(id, site, 2, 0, true, true, false),
                                        "Interrupted drink resets");
                            yield game.act(id, site, 2, 0, true, true, false);
                        }
                        case HAY -> {
                            for (int i = 0; i < 2; i++) {
                                game.act(id, site, 1, 1, true, false, false);
                                for (int j = 0; j < 4; j++)
                                    require(
                                            !game.act(id, site, 1, 1, true, true, false),
                                            "A held key counts once");
                            }
                            game.act(id, site, 1, 1, true, false, false);
                            yield game.act(id, site, 1, 1, true, true, false);
                        }
                        case TREE -> {
                            game.act(id, site, 3, 0, true, false, false);
                            for (int i = 0; i < 4; i++) {
                                game.act(id, site, 0, 3, true, false, false);
                                require(
                                        !game.act(id, site, 3, 0, true, false, false),
                                        "Backtracking is not a lap");
                            }
                            // A hider can walk this close to the one-block trunk, even at the
                            // corners.
                            boolean lap = false;
                            for (int n = 1; n <= 3; n++) {
                                for (int step = 0; step <= 96; step++) {
                                    double angle =
                                            (clockwise == (n % 2 == 1) ? 1 : -1)
                                                    * step
                                                    * 2
                                                    * Math.PI
                                                    / 96;
                                    lap =
                                            game.act(
                                                    id,
                                                    site,
                                                    1.2 * Math.cos(angle),
                                                    1.2 * Math.sin(angle),
                                                    true,
                                                    false,
                                                    false);
                                    require(
                                            lap == (n == 3 && step == 96),
                                            "Exactly three full laps in either direction");
                                }
                            }
                            yield lap;
                        }
                        case TROUGH -> {
                            require(
                                    !game.act(id, site, -3, 0, true, false, false)
                                            && !game.act(id, site, 4, 0, true, false, false),
                                    "Walking beside the bowls cannot score");
                            require(
                                    !game.stepOnBowl(id, -1) && !game.stepOnBowl(id, 6),
                                    "Only actual bowls count");
                            for (int i = 5; i > 0; i--) {
                                require(
                                        !game.stepOnBowl(id, i) && !game.stepOnBowl(id, i),
                                        "Visit distinct bowls");
                                require(
                                        game.bowlsVisited(id) == 6 - i,
                                        "Repeated steps count once");
                            }
                            yield game.stepOnBowl(id, 0);
                        }
                        case BELL -> {
                            game.act(id, site, 12, 0, true, false, true);
                            require(
                                    !game.act(id, site, 2, 0, true, false, true),
                                    "Ring on a fresh jump at the bell");
                            game.act(id, site, 2, 0, true, false, false);
                            yield game.act(id, site, 2, 0, true, false, true);
                        }
                        case PARCEL -> {
                            require(
                                    game.click(id, FurryQuest.Station.LOOM, false, 0)
                                            == FurryQuest.Interaction.IGNORED,
                                    "No delivery before pickup");
                            require(
                                    game.click(id, FurryQuest.Station.BARN_BARREL, false, 1)
                                            == FurryQuest.Interaction.NEXT,
                                    "Pick up wool at the barn");
                            game.woolTick(id, true);
                            game.woolTick(id, false);
                            require(game.woolTicks(id) == -1, "Walking away interrupts collection");
                            game.click(id, FurryQuest.Station.BARN_BARREL, false, 2);
                            require(
                                    workWool(game, id) == FurryQuest.Interaction.NEXT,
                                    "Collect for five seconds");
                            require(
                                    game.station(id, site) == FurryQuest.Station.LOOM,
                                    "Keep the parcel while crossing the farm");
                            game.click(id, FurryQuest.Station.LOOM, false, 122);
                            yield workWool(game, id) == FurryQuest.Interaction.COMPLETE;
                        }
                        case FISH -> {
                            var post = FurryQuest.Station.DOCK_POST;
                            require(
                                    game.click(id, post, false, 10) == FurryQuest.Interaction.CAST,
                                    "Cast at the dock");
                            require(
                                    game.click(id, post, false, 11) == FurryQuest.Interaction.EARLY,
                                    "Early reel loses the bite");
                            game.click(id, post, false, 12);
                            long late = game.biteTick(id) + FurryQuest.BITE_WINDOW;
                            require(
                                    game.click(id, post, false, late)
                                            == FurryQuest.Interaction.CAST,
                                    "Missed bite allows recast");
                            for (int n = 1; n <= 2; n++) {
                                long bite = game.biteTick(id);
                                require(
                                        game.click(id, post, false, bite)
                                                        == FurryQuest.Interaction.NEXT
                                                && game.repetitions(id, site) == n,
                                        "Count one fish per bite");
                                require(
                                        game.click(id, post, false, bite + 1)
                                                == FurryQuest.Interaction.CAST,
                                        "Cast again");
                            }
                            yield game.click(id, post, false, game.biteTick(id))
                                    == FurryQuest.Interaction.COMPLETE;
                        }
                        case CHIMES -> {
                            game.click(id, FurryQuest.Station.BLUE, false, 0);
                            require(
                                    game.click(id, FurryQuest.Station.RED, false, 1)
                                            == FurryQuest.Interaction.WRONG_NOTE,
                                    "Incorrect note resets the tune");
                            for (var note :
                                    List.of(FurryQuest.Station.BLUE, FurryQuest.Station.GOLD))
                                require(
                                        game.click(id, note, false, 2)
                                                == FurryQuest.Interaction.NEXT,
                                        "Play notes in order");
                            yield game.click(id, FurryQuest.Station.RED, false, 3)
                                    == FurryQuest.Interaction.COMPLETE;
                        }
                        case SWAP -> {
                            require(
                                    game.click(id, FurryQuest.Station.SCARECROW, false, 1)
                                            == FurryQuest.Interaction.IGNORED,
                                    "Stand beside the scarecrow without swapping");
                            yield game.click(id, FurryQuest.Station.SCARECROW, true, 2)
                                    == FurryQuest.Interaction.COMPLETE;
                        }
                    };
            require(!game.unfinished(id, site), "Completed tasks leave the active guide");
            require(
                    done && game.completed(id) == seen.size(),
                    "One personal completion for " + site);
        }
        require(seen.size() == 4, "All four different tasks");
        require(
                seen.stream().filter(site -> site.difficulty == FurryQuest.Difficulty.HARD).count()
                        == 1,
                "Every four-task route has exactly one Hard task");
        return seen;
    }

    public static void main(String[] args) {
        checkChimeLayout();
        require(
                FurryQuest.Site.HAY.difficulty == FurryQuest.Difficulty.EASY,
                "Foraging at hay bales is low exposure");
        require(
                FurryQuest.Site.CONTACT.difficulty == FurryQuest.Difficulty.HARD,
                "Loitering by the visible agent is high exposure");
        require(
                FurryQuest.Site.BELL.difficulty == FurryQuest.Difficulty.HARD,
                "Ringing the bell is high exposure");
        var pool = EnumSet.noneOf(FurryQuest.Site.class);
        for (int seed = 0; seed < 32; seed++) {
            var sample = new FurryQuest(PLAYERS, new Random(seed));
            for (UUID id : PLAYERS)
                if (!id.equals(sample.sniper())) pool.addAll(finishRoute(sample, id, true));
        }
        require(
                pool.equals(EnumSet.allOf(FurryQuest.Site.class)),
                "Every mission can appear and be completed");
        checkInterleavedTasks();
        FurryQuest game = round();
        UUID sniper = game.sniper();
        List<UUID> spies = PLAYERS.stream().filter(id -> !id.equals(sniper)).toList();
        UUID first = spies.getFirst(), second = spies.getLast();
        require(
                game.remaining() == 2 && !game.unfinished(sniper, FurryQuest.Site.TREE),
                "One sniper and two agents");
        finishRoute(game, first, true);
        require(
                game.outcome() == null && game.completed(second) == 0,
                "Personal tasks cannot be pooled");
        require(!game.startFinalHuntIfReady(), "Wait for every living agent");
        require(
                !game.act(first, FurryQuest.Site.BELL, 0, 0, true, true, true),
                "Completed agents cannot score again");
        finishRoute(game, second, false);
        require(
                game.startFinalHuntIfReady() && game.outcome() == null,
                "Last route starts the final hunt");
        game.timeout();
        require(
                game.outcome() == FurryQuest.Outcome.MISSIONS
                        && Set.copyOf(game.result(Set.copyOf(PLAYERS)).places().getFirst())
                                .equals(Set.copyOf(spies)),
                "All surviving agents share first place");

        game = round();
        finishRoute(game, second, false);
        game.out(first);
        require(game.startFinalHuntIfReady(), "Losing the last unfinished agent starts the hunt");
        game.timeout();
        require(
                game.result(Set.copyOf(PLAYERS)).places().equals(List.of(List.of(second))),
                "A caught agent with unfinished tasks cannot block or share the mission win");

        game = round();
        game.out(first);
        finishRoute(game, second, false);
        require(
                game.startFinalHuntIfReady(),
                "Living agents can finish after an earlier elimination");
        game.out(second);
        require(
                game.outcome() == FurryQuest.Outcome.ALL_CAUGHT && !game.startFinalHuntIfReady(),
                "Catching the last agent during the countdown wins immediately");
        game.timeout();
        require(
                game.result(Set.copyOf(PLAYERS)).places().equals(List.of(List.of(sniper))),
                "Sniper keeps the win");

        game = round();
        finishRoute(game, first, true);
        finishRoute(game, second, false);
        require(game.startFinalHuntIfReady(), "Start the hunt with two agents");
        game.out(first);
        require(
                !game.startFinalHuntIfReady() && game.outcome() == null,
                "A catch cannot restart the ten seconds");
        game.timeout();
        require(
                game.result(Set.copyOf(PLAYERS)).places().equals(List.of(List.of(second))),
                "Only agents still alive at the end win");
        game = round();
        for (int mistake = 1; mistake <= 3; mistake++) {
            game.wrongShot();
            require(
                    game.outcome() == null && game.mistakesLeft() == 3 - mistake,
                    "Three mistakes are allowed");
        }
        game.wrongShot();
        require(
                game.result(Set.copyOf(PLAYERS)).places().getFirst().containsAll(spies),
                "Livestock loss");
        game = round();
        game.timeout();
        require(game.outcome() == FurryQuest.Outcome.TIME, "Round timer");
        game = round();
        game.out(sniper);
        require(game.outcome() == FurryQuest.Outcome.SNIPER_LEFT, "Sniper departure");
        var session = new FurryQuestSession(null, null, PartyRoundContext.standalone(null));
        session.restartRoundTimer(session.durationSeconds());
        session.tick = 2999;
        require(session.remainingRoundTicks() == 1, "One tick remains on the original clock");
        session.restartRoundTimer(FurryQuest.FINAL_SECONDS);
        session.tick += 199;
        require(
                session.remainingRoundTicks() == 1,
                "The sniper gets the full ten seconds across the old deadline");
        session.tick++;
        require(session.remainingRoundTicks() == 0, "Final hunt ends on tick 200");
        System.out.println(
                "Furry Quest personal routes, final countdown and survivor awards passed");
    }

    private static void checkChimeLayout() {
        var bell = FurryQuest.Site.BELL.plot;
        var chimes = FurryQuest.Site.CHIMES.stops;
        for (var chime : chimes) {
            for (double x : new double[] {-2.25, 2.25}) {
                for (double z : new double[] {-2.25, 2.25}) {
                    double bx = bell.x() - x, bz = bell.z() - z;
                    double cx = chime.x - x, cz = chime.z - z;
                    double dot = (bx * cx + bz * cz) / (Math.hypot(bx, bz) * Math.hypot(cx, cz));
                    require(
                            dot < Math.cos(Math.toRadians(130)),
                            "Hunter must turn away from the bell at every deck corner");
                }
            }
            for (var other : chimes)
                if (other != chime)
                    require(
                            Math.hypot(chime.x - other.x, chime.z - other.z) >= 6,
                            "Each note requires moving to a different chime");
        }
    }

    private static FurryQuest.Interaction workWool(FurryQuest game, UUID id) {
        for (int tick = 1; tick < 100; tick++)
            require(
                    game.woolTick(id, true) == FurryQuest.Interaction.IGNORED,
                    "Wool needs a full five seconds");
        return game.woolTick(id, true);
    }

    private static void checkInterleavedTasks() {
        var players = PLAYERS.subList(0, 2);
        // Deals wool, hat swap, chimes and oak laps.
        var game = new FurryQuest(players, new Random(85));
        UUID id = players.stream().filter(p -> !p.equals(game.sniper())).findFirst().orElseThrow();
        require(
                game.click(id, FurryQuest.Station.BARN_BARREL, false, 0)
                        == FurryQuest.Interaction.NEXT,
                "Collect a parcel before starting the tune");
        require(
                workWool(game, id) == FurryQuest.Interaction.NEXT,
                "Wool is ready to take to the loom");
        require(
                game.click(id, FurryQuest.Station.BLUE, false, 1) == FurryQuest.Interaction.NEXT,
                "Begin the tune");
        require(
                game.click(id, FurryQuest.Station.SCARECROW, true, 2)
                        == FurryQuest.Interaction.COMPLETE,
                "Swap the hat while the tune and parcel are unfinished");
        require(
                game.click(id, FurryQuest.Station.GOLD, false, 2) == FurryQuest.Interaction.NEXT
                        && game.click(id, FurryQuest.Station.RED, false, 3)
                                == FurryQuest.Interaction.COMPLETE,
                "Resume a partially played tune");
        require(
                game.click(id, FurryQuest.Station.LOOM, false, 4) == FurryQuest.Interaction.NEXT
                        && workWool(game, id) == FurryQuest.Interaction.COMPLETE,
                "Deliver the original parcel after completing other tasks");
        require(
                game.click(id, FurryQuest.Station.DOCK_POST, false, 5)
                                == FurryQuest.Interaction.IGNORED
                        && game.completed(id) == 3,
                "Unassigned tasks cannot score");
    }
}
