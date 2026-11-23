package me.petr1furious.potatominigames.games.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;

final class FurryQuestArena {
    static final int FLOOR = 70;
    static final int EDGE = 22;
    static final int DECK = 9;
    private static final int[] FLEE_TURNS = {0, 1, -1, 2, -2, 3, -3, 4};
    private static final List<FurryQuest.Plot> PATROL = List.of(
            new FurryQuest.Plot(-6, -5), new FurryQuest.Plot(7, -5),
            new FurryQuest.Plot(7, 5), new FurryQuest.Plot(-6, 5));
    private static final List<EntityType> SPECIES =
            List.of(EntityType.SHEEP, EntityType.COW, EntityType.PIG, EntityType.CHICKEN, EntityType.GOAT);

    private static final class Walker {
        final Animals body;
        boolean claimed;
        Location destination;
        long nextWander;
        Location fleeFrom;
        long fleeUntil;

        Walker(Animals body, int delay) {
            this.body = body;
            nextWander = delay;
        }
    }

    private final World world;
    private final Random random;
    private final Map<UUID, Walker> herd = new LinkedHashMap<>();
    private Animals doubleAgent;
    private int waypoint;
    private long nextPatrolCheck;
    private Location patrolSample;
    private int stalledChecks;

    FurryQuestArena(World world, Random random) {
        this.world = world;
        this.random = random;
    }

    void build() {
        new FurryQuestFarm(world).build();
        int contactIndex = random.nextInt(40);
        waypoint = random.nextInt(PATROL.size());
        for (int i = 0; i < 40; i++) {
            Animals body = (Animals) world.spawnEntity(i == contactIndex ? patrolTarget() : meadow(), SPECIES.get(i % SPECIES.size()));
            body.setAdult();
            body.setAgeLock(true);
            body.setInvulnerable(true);
            body.setRemoveWhenFarAway(false);
            herd.put(body.getUniqueId(), new Walker(body, random.nextInt(100)));
            if (i == contactIndex) {
                doubleAgent = body;
                body.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(.14);
                nextWaypoint();
            }
        }
    }

    Location lookout() {
        return new Location(world, .5, FLOOR + DECK + 2, .5, 0, 25);
    }

    boolean belowLookout(Location feet) {
        // The outer ring is half a block lower than the center of the deck.
        return feet.getY() < FLOOR + DECK + 1.5;
    }

    Location staging(int index, int count) {
        double angle = 2 * Math.PI * index / Math.max(1, count);
        return lookout().add(1.4 * Math.sin(angle), 0, 1.4 * Math.cos(angle));
    }

    void constrainSniper(org.bukkit.event.player.PlayerMoveEvent event) {
        Location to = event.getTo();
        double x = Math.clamp(to.getX(), -1.75, 2.75);
        double z = Math.clamp(to.getZ(), -1.75, 2.75);
        if (x != to.getX() || z != to.getZ()) {
            Location edge = to.clone();
            edge.setX(x);
            edge.setZ(z);
            event.setTo(edge);
        }
    }

    Location landmark(FurryQuest.Site site) {
        if (site == FurryQuest.Site.CONTACT) return doubleAgent.getLocation();
        return new Location(world, site.plot.x() + .5, FLOOR + 1, site.plot.z() + .5);
    }

    Location station(FurryQuest.Station station) {
        return new Location(world, station.x + .5, FLOOR + station.y, station.z + .5);
    }

    FurryQuest.Station stationAt(Block block) {
        for (var station : FurryQuest.Station.values()) {
            if (block.getX() == station.x && block.getY() == FLOOR + station.y && block.getZ() == station.z)
                return station;
        }
        return null;
    }

    int bowlAt(Location feet) {
        Block support = feet.clone().subtract(0, .01, 0).getBlock();
        var row = FurryQuest.Site.TROUGH.plot;
        if (support.getY() != FLOOR + 1 || support.getZ() != row.z()
                || support.getType() != Material.WATER_CAULDRON) return -1;
        return support.getX() - row.x() - FurryQuest.FIRST_BOWL_OFFSET;
    }

    private boolean open(int x, int z) {
        return Math.abs(x) < EDGE && Math.abs(z) < EDGE
                && switch (world.getBlockAt(x, FLOOR, z).getType()) {
                    case GRASS_BLOCK, COARSE_DIRT, GRAVEL, OAK_PLANKS, ANDESITE, MOSS_BLOCK -> true;
                    default -> false;
                }
                && world.getBlockAt(x, FLOOR + 1, z).isEmpty()
                && world.getBlockAt(x, FLOOR + 2, z).isEmpty();
    }

    /** Find a clear approach for livestock visiting a landmark. */
    private Location destination(Block clicked) {
        int x = clicked.getX(), z = clicked.getZ();
        if (Math.abs(x) >= EDGE || Math.abs(z) >= EDGE) return null;
        Location nearest = null;
        int best = Integer.MAX_VALUE;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int distance = dx * dx + dz * dz;
                if (distance < best && open(x + dx, z + dz)) {
                    best = distance;
                    nearest = new Location(world, x + dx + .5, FLOOR + 1, z + dz + .5);
                }
            }
        }
        return nearest;
    }

    private Location meadow() {
        int x, z;
        do {
            x = random.nextInt(1 - EDGE, EDGE);
            z = random.nextInt(1 - EDGE, EDGE);
        } while (!open(x, z));
        return new Location(world, x + .5, FLOOR + 1, z + .5);
    }

    Animals doubleAgent() { return doubleAgent; }

    List<Animals> shuffledDisguises() {
        var animals = new ArrayList<>(herd.values().stream().map(w -> w.body).filter(a -> a != doubleAgent).toList());
        Collections.shuffle(animals, random);
        return animals;
    }

    boolean contains(UUID id) { return herd.containsKey(id); }

    private void walk(Walker walker, Location target, long tick) {
        walker.destination = target;
        walker.nextWander = tick + 600;
        walker.body.getPathfinder().moveTo(target, 1.0);
    }

    void claim(Animals animal) {
        herd.get(animal.getUniqueId()).claimed = true;
    }

    private Location patrolTarget() {
        var point = PATROL.get(waypoint);
        return new Location(world, point.x() + .5, FLOOR + 1, point.z() + .5);
    }

    private void nextWaypoint() {
        waypoint = (waypoint + 1) % PATROL.size();
    }

    private void patrol(long tick) {
        Location here = doubleAgent.getLocation();
        if (here.distanceSquared(patrolTarget()) < 1.5) {
            nextWaypoint();
            stalledChecks = 0;
        }
        if (tick >= nextPatrolCheck) {
            if (patrolSample != null && here.distanceSquared(patrolSample) < .25) {
                nextWaypoint();
                // Retry a different leg first; recover a mob physically wedged in scenery.
                if (++stalledChecks >= 2) {
                    doubleAgent.teleport(patrolTarget());
                    nextWaypoint();
                    stalledChecks = 0;
                }
            } else stalledChecks = 0;
            patrolSample = doubleAgent.getLocation();
            nextPatrolCheck = tick + 40;
        }
        doubleAgent.getPathfinder().moveTo(patrolTarget(), .65);
    }

    void scatter(Location impact, long tick) {
        for (Walker walker : herd.values()) {
            if (walker.claimed || walker.body.getLocation().distanceSquared(impact) > 8 * 8) continue;
            walker.fleeFrom = impact;
            walker.fleeUntil = tick + 60;
            flee(walker);
        }
    }

    private void flee(Walker walker) {
        Location here = walker.body.getLocation();
        double dx = here.getX() - walker.fleeFrom.getX(), dz = here.getZ() - walker.fleeFrom.getZ();
        double away = dx * dx + dz * dz < .01 ? random.nextDouble() * 2 * Math.PI : Math.atan2(dz, dx);
        var navigation = walker.body.getPathfinder();
        // Try sideways exits too, so animals at a fence can run along it.
        for (int turn : FLEE_TURNS) {
            double angle = away + turn * Math.PI / 4;
            int x = (int) Math.floor(here.getX() + 6 * Math.cos(angle));
            int z = (int) Math.floor(here.getZ() + 6 * Math.sin(angle));
            Location target = destination(world.getBlockAt(x, FLOOR, z));
            if (target == null || target.distanceSquared(here) < 4) continue;
            var path = navigation.findPath(target);
            if (path != null && path.getFinalPoint() != null && path.getFinalPoint().distanceSquared(target) < 2.25
                    && navigation.moveTo(path, 2.0)) {
                walker.destination = target;
                return;
            }
        }
        walker.destination = null;
    }

    void tick(long tick) {
        for (Walker walker : herd.values()) {
            if (walker.claimed) continue;
            if (walker.fleeFrom != null) {
                if (tick < walker.fleeUntil) {
                    if (tick % 5 == 0 && (walker.destination == null || !walker.body.getPathfinder().hasPath()
                            || walker.body.getLocation().distanceSquared(walker.destination) < 2.25)) flee(walker);
                    continue;
                }
                walker.body.getPathfinder().stopPathfinding();
                walker.fleeFrom = null;
                walker.destination = null;
                walker.nextWander = tick + 20;
                if (walker.body == doubleAgent) {
                    patrolSample = null;
                    stalledChecks = 0;
                }
            }
            if (tick % 10 != 0) continue;
            if (walker.body == doubleAgent) {
                patrol(tick);
                continue;
            }
            if (walker.destination != null && walker.body.getLocation().distanceSquared(walker.destination) < 2.25) {
                walker.destination = null;
                walker.nextWander = tick + 60 + random.nextInt(100);
            }
            if (tick >= walker.nextWander) {
                // Livestock also visit the mission landmarks and pause between walks.
                Location target = meadow();
                if (random.nextInt(3) == 0) {
                    var sites = FurryQuest.Site.values();
                    var site = sites[random.nextInt(sites.length)];
                    target = destination(landmark(site).getBlock());
                }
                if (target != null) walk(walker, target, tick);
            } else if (walker.destination != null) {
                // A vanilla behaviour (such as grazing) may have interrupted the route.
                var path = walker.body.getPathfinder().getCurrentPath();
                if (path == null || path.getFinalPoint() == null
                        || path.getFinalPoint().distanceSquared(walker.destination) > 2.25)
                    walker.body.getPathfinder().moveTo(walker.destination, 1.0);
            }
        }
    }

    void remove(Animals animal) {
        herd.remove(animal.getUniqueId());
        animal.remove();
    }

    void cleanup() {
        herd.values().forEach(walker -> walker.body.remove());
        herd.clear();
    }
}
