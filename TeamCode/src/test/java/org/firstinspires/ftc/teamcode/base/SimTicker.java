package org.firstinspires.ftc.teamcode.base;

/**
 * The grid every periodic thing in a run happens on.
 *
 * <p>The clock is read once when the run starts, and that reading is the origin:
 * every instant after it is the origin plus a whole number of
 * {@value #GRID_MS} ms periods. So the time one pass takes is charged to that
 * pass and to nothing after it, and a run that falls behind does not drift.
 *
 * <p>Pure, and it reads no clock of its own: {@link SimRun} reads the clock and
 * hands the readings in. Which means the whole of the timing can be tested
 * without waiting for anything.
 *
 * <p>Passes when: SimTickerTest.
 */
final class SimTicker {

    private SimTicker() {
    }

    /** One instant to the next, in milliseconds. */
    static final long GRID_MS = 10;

    /** Reading the gamepads and stepping the simulated robot, in instants. */
    static final long STEP_TICKS = 1;

    /** Watching an unclaimed gamepad for Start and A, in instants: 50 ms. */
    static final long GESTURE_TICKS = 5;

    /** Printing what changed and what is off rest, in instants: 250 ms. */
    static final long REPORT_TICKS = 25;

    /** The most simulated time one step is allowed to be worth, in milliseconds. */
    static final long MAX_STEP_MS = 100;

    /** Which instant a clock reading falls on, counting from the origin. */
    static long tickAt(long originMs, long nowMs) {
        return Math.floorDiv(nowMs - originMs, GRID_MS);
    }

    /** When an instant begins, by the same clock the origin was read from. */
    static long startOf(long originMs, long tick) {
        return originMs + tick * GRID_MS;
    }

    /**
     * How much simulated time a step is worth, given how long the pass before it
     * took in real milliseconds.
     *
     * <p>The pass in progress cannot know its own cost, so what a step is handed
     * is always the pass before it: whatever the OpMode's loop spends lands on
     * the next step, wherever in the loop it was spent. Simulated time is
     * therefore one pass behind real time, by a constant rather than a growing
     * amount.
     *
     * <p>Bounded at {@value #MAX_STEP_MS} ms, so a pass held up by a suspended
     * laptop or a long collection does not integrate that whole span in one
     * stride; the run falls behind instead. Bounded below at 0, because a clock
     * that went backwards must not move the robot backwards.
     */
    static long stepWorth(long elapsedMs) {
        return Math.min(MAX_STEP_MS, Math.max(0, elapsedMs));
    }

    /**
     * True when a job whose period is {@code ticks} is due at {@code tick},
     * given that it last ran at {@code lastTick}.
     *
     * <p>Once per period, not once per period that went by. A pass that takes
     * 40 ms skips the three instants it ran through, and a job it missed runs
     * once at the instant the pass ended rather than three times in a row.
     *
     * <p>Pass {@code -1} as {@code lastTick} for a job that has not run yet, and
     * every job is due at the first instant.
     */
    static boolean due(long tick, long lastTick, long ticks) {
        return Math.floorDiv(tick, ticks) != Math.floorDiv(lastTick, ticks);
    }
}
