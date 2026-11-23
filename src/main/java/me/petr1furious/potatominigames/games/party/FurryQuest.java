package me.petr1furious.potatominigames.games.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Each surviving agent must finish a personal route to start the sniper's final countdown. */
final class FurryQuest {
    static final int SECONDS = 150;
    static final int FINAL_SECONDS = 10;
    private static final int CONTACT_TICKS = 15 * 20;

    enum Station {
        BARN_BARREL(16, 1, -14), LOOM(-15, 1, 3), DOCK_POST(-11, 1, -9),
        BLUE(-8, 1, -17), GOLD(0, 1, -18), RED(6, 1, -16), SCARECROW(18, 3, 16);

        final int x, y, z;
        Station(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Site task() {
            return switch (this) {
                case BARN_BARREL, LOOM -> Site.PARCEL;
                case DOCK_POST -> Site.FISH;
                case BLUE, GOLD, RED -> Site.CHIMES;
                case SCARECROW -> Site.SWAP;
            };
        }

        boolean chime() { return this == BLUE || this == GOLD || this == RED; }
    }

    record Plot(int x, int z) {}

    /** How clearly the task gives away a hider to the sniper. */
    enum Difficulty { EASY, MEDIUM, HARD }

    enum Site {
        POND(Difficulty.EASY, -13, -10), HAY(Difficulty.EASY, 12, -11),
        TREE(Difficulty.MEDIUM, -12, 10), TROUGH(Difficulty.EASY, 13, 8), BELL(Difficulty.HARD, 1, 15),
        PARCEL(Difficulty.MEDIUM, Station.BARN_BARREL, Station.LOOM), FISH(Difficulty.MEDIUM, Station.DOCK_POST),
        CHIMES(Difficulty.HARD, Station.BLUE, Station.GOLD, Station.RED),
        SWAP(Difficulty.MEDIUM, Station.SCARECROW), CONTACT(Difficulty.HARD);

        final Difficulty difficulty;
        final Plot plot;
        final List<Station> stops;
        Site(Difficulty difficulty, Plot plot, List<Station> stops) {
            this.difficulty = difficulty;
            this.plot = plot;
            this.stops = stops;
        }
        Site(Difficulty difficulty) {
            this(difficulty, null, List.of()); // The contact moves with the herd.
        }
        Site(Difficulty difficulty, int x, int z) {
            this(difficulty, new Plot(x, z), List.of());
        }
        Site(Difficulty difficulty, Station... stops) {
            this(difficulty, new Plot(stops[0].x, stops[0].z), List.of(stops));
        }
    }

    static final int TASKS = 4;
    static final int REPEATS = 3;
    static final int WOOL_TICKS = 5 * 20;
    static final int BITE_WINDOW = 20;
    enum Interaction { IGNORED, NEXT, CAST, EARLY, WRONG_NOTE, COMPLETE }
    enum Outcome { MISSIONS, LIVESTOCK, ALL_CAUGHT, TIME, SNIPER_LEFT }

    private static final class Route {
        final List<Site> sites;
        final Random random;
        boolean active = true;
        final Map<Site, TaskProgress> pending = new EnumMap<>(Site.class);

        Route(Random random) {
            this.random = random;
            var hard = new ArrayList<Site>();
            var others = new ArrayList<Site>();
            for (Site site : Site.values()) {
                if (site.difficulty == Difficulty.HARD) hard.add(site);
                else others.add(site);
            }
            Collections.shuffle(others, random);
            var route = new ArrayList<>(others.subList(0, TASKS - 1));
            route.add(hard.get(random.nextInt(hard.size())));
            Collections.shuffle(route, random);
            sites = List.copyOf(route);
            for (Site site : sites) pending.put(site, new TaskProgress(site));
        }

        void complete(Site site) {
            pending.remove(site);
        }
    }

    private static final class TaskProgress {
        final Site site;
        int progress;
        int repetitions;
        int woolTicks = -1;
        double previousAngle = Double.NaN;
        double turn;
        long biteTick = -1;
        boolean crouching, jumping;

        TaskProgress(Site site) { this.site = site; }

        Station station() {
            return site.stops.isEmpty() ? null : site.stops.get(progress);
        }

        Interaction click(Station clicked, boolean sneak, long tick, Random random) {
            Station expected = station();
            if (expected == null || clicked == null) return Interaction.IGNORED;
            if (clicked != expected) {
                if (site == Site.CHIMES && clicked.chime()) {
                    progress = 0;
                    return Interaction.WRONG_NOTE;
                }
                return Interaction.IGNORED;
            }
            if (site == Site.SWAP && !sneak) return Interaction.IGNORED;
            if (site == Site.PARCEL) {
                if (woolTicks >= 0) return Interaction.IGNORED;
                woolTicks = 0;
                return Interaction.NEXT;
            }
            if (site == Site.FISH) {
                if (biteTick < 0 || tick >= biteTick + BITE_WINDOW) {
                    biteTick = tick + 24 + random.nextInt(33);
                    return Interaction.CAST;
                }
                if (tick < biteTick) {
                    biteTick = -1;
                    return Interaction.EARLY;
                }
                biteTick = -1;
                return ++repetitions == REPEATS ? Interaction.COMPLETE : Interaction.NEXT;
            }
            if (++progress < site.stops.size()) return Interaction.NEXT;
            return Interaction.COMPLETE;
        }

        boolean act(double dx, double dz, boolean level, boolean crouch, boolean jump) {
            boolean peck = crouch && !crouching, ring = jump && !jumping;
            crouching = crouch;
            jumping = jump;
            if (!site.stops.isEmpty()) return false;
            double radius2 = dx * dx + dz * dz;
            if (!level || radius2 > (site == Site.TREE || site == Site.TROUGH ? 30.25 : 16)) {
                progress = 0;
                previousAngle = Double.NaN;
                turn = 0;
                return false;
            }
            return switch (site) {
                case POND -> {
                    progress = crouch ? progress + 1 : 0;
                    yield progress >= 40;
                }
                case HAY -> peck && ++progress == 3;
                case TREE -> circle(dx, dz);
                case TROUGH -> {
                    if (Math.abs(dz) <= 2) {
                        if (dx <= -2.75) progress |= 1;
                        if (dx >= 3.75) progress |= 2;
                    }
                    yield progress == 3;
                }
                case BELL -> ring;
                case PARCEL, FISH, CHIMES, SWAP, CONTACT -> false;
            };
        }

        private boolean circle(double dx, double dz) {
            double angle = Math.atan2(dz, dx);
            if (!Double.isNaN(previousAngle)) {
                double delta = Math.IEEEremainder(angle - previousAngle, 2 * Math.PI);
                turn = Math.abs(delta) > Math.PI / 2 + 1e-6 ? 0 : turn + delta;
            }
            previousAngle = angle;
            if (Math.abs(turn) + 1e-6 < 2 * Math.PI) return false;
            turn -= Math.copySign(2 * Math.PI, turn);
            return ++repetitions == REPEATS;
        }
    }

    private final UUID sniper;
    private final Map<UUID, Route> routes = new LinkedHashMap<>();
    private Outcome outcome;
    private boolean finalHunt;

    FurryQuest(Collection<UUID> players, Random random) {
        if (players.size() < 2) throw new IllegalArgumentException("Furry Quest requires two players");
        var ids = new ArrayList<>(players);
        sniper = ids.remove(random.nextInt(ids.size()));
        for (UUID id : ids) routes.put(id, new Route(random));
    }

    UUID sniper() { return sniper; }
    int remaining() { return (int) routes.values().stream().filter(r -> r.active).count(); }
    int completed(UUID id) { return TASKS - routes.get(id).pending.size(); }
    List<Site> assignments(UUID id) { return routes.get(id).sites; }

    boolean unfinished(UUID id, Site site) {
        return pending(id, site) != null;
    }

    int repetitions(UUID id, Site site) {
        return routes.get(id).pending.get(site).repetitions;
    }

    int woolTicks(UUID id) {
        TaskProgress task = pending(id, Site.PARCEL);
        return task == null ? -1 : task.woolTicks;
    }

    Interaction woolTick(UUID id, boolean nearby) {
        TaskProgress task = pending(id, Site.PARCEL);
        if (task == null || task.woolTicks < 0) return Interaction.IGNORED;
        if (!nearby) {
            task.woolTicks = -1;
            return Interaction.IGNORED;
        }
        if (++task.woolTicks < WOOL_TICKS) return Interaction.IGNORED;
        task.woolTicks = -1;
        if (++task.progress < Site.PARCEL.stops.size()) return Interaction.NEXT;
        routes.get(id).complete(Site.PARCEL);
        return Interaction.COMPLETE;
    }

    private TaskProgress pending(UUID id, Site site) {
        Route route = routes.get(id);
        return outcome == null && route != null && route.active ? route.pending.get(site) : null;
    }

    Outcome outcome() { return outcome; }

    int contactSecondsLeft(UUID id) {
        TaskProgress task = pending(id, Site.CONTACT);
        return task == null ? 0 : (CONTACT_TICKS - task.progress + 19) / 20;
    }

    boolean contactTick(UUID id, double distanceSquared) {
        TaskProgress task = pending(id, Site.CONTACT);
        if (task == null) return false;
        task.progress = distanceSquared <= 16 ? task.progress + 1 : 0;
        if (task.progress < CONTACT_TICKS) return false;
        routes.get(id).complete(Site.CONTACT);
        return true;
    }

    Station station(UUID id, Site site) {
        TaskProgress task = pending(id, site);
        return task == null ? null : task.station();
    }

    long biteTick(UUID id) {
        TaskProgress task = pending(id, Site.FISH);
        return task == null ? -1 : task.biteTick;
    }

    Interaction click(UUID id, Station station, boolean sneak, long tick) {
        if (station == null) return Interaction.IGNORED;
        Site site = station.task();
        TaskProgress task = pending(id, site);
        if (task == null) return Interaction.IGNORED;
        Route route = routes.get(id);
        Interaction result = task.click(station, sneak, tick, route.random);
        if (result == Interaction.COMPLETE) route.complete(site);
        return result;
    }

    boolean finalHunt() { return finalHunt; }

    boolean startFinalHuntIfReady() {
        if (finalHunt || outcome != null || routes.values().stream().anyMatch(r -> r.active && !r.pending.isEmpty()))
            return false;
        finalHunt = true;
        return true;
    }

    boolean act(UUID id, Site site, double dx, double dz, boolean level, boolean crouch, boolean jump) {
        TaskProgress task = pending(id, site);
        if (task == null || !task.act(dx, dz, level, crouch, jump)) return false;
        routes.get(id).complete(site);
        return true;
    }

    void out(UUID id) {
        if (outcome != null) return;
        if (sniper.equals(id)) outcome = Outcome.SNIPER_LEFT;
        else if (routes.containsKey(id)) {
            routes.get(id).active = false;
            if (remaining() == 0) outcome = Outcome.ALL_CAUGHT;
        }
    }

    void wrongShot() {
        if (outcome == null) outcome = Outcome.LIVESTOCK;
    }

    void timeout() {
        if (outcome == null) outcome = finalHunt ? Outcome.MISSIONS : Outcome.TIME;
    }

    PartyRoundResult result(Set<UUID> eligible) {
        if (outcome == null) return PartyRoundResult.empty();
        List<UUID> winners = switch (outcome) {
            case ALL_CAUGHT, TIME -> List.of(sniper);
            case MISSIONS, LIVESTOCK, SNIPER_LEFT -> routes.entrySet().stream()
                    .filter(e -> !finalHunt || e.getValue().active).map(Map.Entry::getKey).toList();
        };
        return new PartyRoundResult(List.of(winners)).eligible(eligible);
    }
}
