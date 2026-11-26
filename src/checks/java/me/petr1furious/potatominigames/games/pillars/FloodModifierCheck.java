package me.petr1furious.potatominigames.games.pillars;

/** The rising plane is independent of movement, arena height and blocks surrounding a player. */
public final class FloodModifierCheck {
    public static void main(String[] args) {
        for (int feet : new int[] {201, 64, -30}) {
            int initial = feet - 20;
            check(standardSurface(feet, 0) == initial, "Start twenty blocks below player feet");
            check(standardSurface(feet, 199) == initial, "No early rise before ten seconds");
            check(standardSurface(feet, 200) == initial + 1, "First rise after exactly 200 ticks");
            check(
                    standardSurface(feet, 399) == initial + 1,
                    "Level persists until the next interval");
            check(
                    standardSurface(feet, 400) == initial + 2,
                    "Consecutive intervals add one block each");
            check(
                    !FloodModifier.touches(feet, standardSurface(feet, 3999)),
                    "An unmoving player is safe until the surface reaches their feet");
            check(
                    FloodModifier.touches(feet, standardSurface(feet, 4000)),
                    "An unmoving player is hit at the twentieth rise, including inside a box");
            check(
                    FloodModifier.touches(initial, initial),
                    "Contact at the exact water surface is lethal");
            check(
                    !FloodModifier.touches(Math.nextUp((double) initial), initial),
                    "Above the surface remains safe");
            check(
                    FloodModifier.touches(initial - 30, initial),
                    "Being below the initial layer is not a refuge");
        }
        check(
                standardSurface(201, Long.MAX_VALUE) == Integer.MAX_VALUE,
                "A long-running flood cannot wrap downwards");
        check(
                FloodModifier.surfaceAfterTicks(201, 59, 7, 60) == 194
                        && FloodModifier.surfaceAfterTicks(201, 60, 7, 60) == 195,
                "Custom depth and rise interval reach the live water-plane calculation");
        System.out.println(
                "Flood: initial height, exact rise boundaries, stationary contact and submerged"
                        + " kills verified");
    }

    private static int standardSurface(int feet, long ticks) {
        return FloodModifier.surfaceAfterTicks(
                feet, ticks, FloodModifier.START_BELOW, FloodModifier.RISE_TICKS);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
