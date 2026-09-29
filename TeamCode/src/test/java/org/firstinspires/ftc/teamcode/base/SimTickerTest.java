package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The grid, with no clock: every reading is a number handed in.
 *
 * <p>What is checked is the two things the grid promises. An instant is the
 * origin plus a whole number of periods, however long a pass took. And a job
 * that was missed runs once at the instant the pass ended, not once for each
 * period that went by while it ran.
 */
public final class SimTickerTest {

    private static final long ORIGIN = 1_000_000L;

    // --- which instant a reading falls on ---------------------------------

    @Test
    public void theOriginIsTheFirstInstant() {
        assertEquals(0, SimTicker.tickAt(ORIGIN, ORIGIN));
        assertEquals(0, SimTicker.tickAt(ORIGIN, ORIGIN + 9));
        assertEquals(1, SimTicker.tickAt(ORIGIN, ORIGIN + 10));
        assertEquals(1, SimTicker.tickAt(ORIGIN, ORIGIN + 19));
    }

    @Test
    public void aReadingBeforeTheOriginIsBeforeTheFirstInstant() {
        assertEquals("a clock that went backwards is not instant 0",
                -1, SimTicker.tickAt(ORIGIN, ORIGIN - 1));
    }

    @Test
    public void anInstantBeginsAWholeNumberOfPeriodsAfterTheOrigin() {
        assertEquals(ORIGIN, SimTicker.startOf(ORIGIN, 0));
        assertEquals(ORIGIN + 10, SimTicker.startOf(ORIGIN, 1));
        assertEquals(ORIGIN + 2500, SimTicker.startOf(ORIGIN, 250));
    }

    /**
     * The grid does not drift: a pass that overran by 37 ms leaves the next
     * instant on the same multiple of 10 ms it always was.
     */
    @Test
    public void aLatePassDoesNotMoveTheGrid() {
        long tick = SimTicker.tickAt(ORIGIN, ORIGIN + 10 + 37);
        assertEquals(4, tick);
        assertEquals(ORIGIN + 50, SimTicker.startOf(ORIGIN, tick + 1));
    }

    // --- which jobs are due ----------------------------------------------

    @Test
    public void everyJobIsDueAtTheFirstInstant() {
        assertTrue("read and step", SimTicker.due(0, -1, SimTicker.STEP_TICKS));
        assertTrue("the gestures", SimTicker.due(0, -1, SimTicker.GESTURE_TICKS));
        assertTrue("the reports", SimTicker.due(0, -1, SimTicker.REPORT_TICKS));
    }

    @Test
    public void readingAndSteppingIsDueAtEveryInstantAndOnlyOncePerInstant() {
        assertTrue(SimTicker.due(1, 0, SimTicker.STEP_TICKS));
        assertTrue(SimTicker.due(2, 1, SimTicker.STEP_TICKS));
        assertFalse("the same instant visited twice steps once",
                SimTicker.due(2, 2, SimTicker.STEP_TICKS));
    }

    @Test
    public void theGesturesAreDueEveryFifthInstant() {
        assertFalse(SimTicker.due(1, 0, SimTicker.GESTURE_TICKS));
        assertFalse(SimTicker.due(4, 0, SimTicker.GESTURE_TICKS));
        assertTrue("50 ms after the last one", SimTicker.due(5, 0, SimTicker.GESTURE_TICKS));
        assertFalse(SimTicker.due(6, 5, SimTicker.GESTURE_TICKS));
        assertTrue(SimTicker.due(10, 5, SimTicker.GESTURE_TICKS));
    }

    @Test
    public void theReportsAreDueEveryTwentyFifthInstant() {
        assertFalse(SimTicker.due(24, 0, SimTicker.REPORT_TICKS));
        assertTrue("250 ms after the last one", SimTicker.due(25, 0, SimTicker.REPORT_TICKS));
        assertFalse(SimTicker.due(49, 25, SimTicker.REPORT_TICKS));
        assertTrue(SimTicker.due(50, 25, SimTicker.REPORT_TICKS));
    }

    /**
     * A pass that took 400 ms runs the gestures once when it ends, not the eight
     * times the eight periods it ran through would ask for.
     */
    @Test
    public void aJobThatWasMissedRunsOnceAndNotOncePerPeriod() {
        long last = 0;
        long tick = 40;
        int runs = 0;
        while (runs < 8 && SimTicker.due(tick, last, SimTicker.GESTURE_TICKS)) {
            runs++;
            last = tick;
        }
        assertEquals("one run, not eight", 1, runs);
    }

    @Test
    public void aJobLateByOneInstantStillRunsOncePerPeriod() {
        assertTrue("it ran at 6 rather than 5, so 10 is its next",
                SimTicker.due(10, 6, SimTicker.GESTURE_TICKS));
        assertFalse("and 9 is not", SimTicker.due(9, 6, SimTicker.GESTURE_TICKS));
    }

    @Test
    public void aStepIsWorthTheRealTimeThePassBeforeItTook() {
        assertEquals("an ordinary pass", 10, SimTicker.stepWorth(10));
        assertEquals("a slow one", 25, SimTicker.stepWorth(25));
        assertEquals("a pass with nothing measurable in it", 0, SimTicker.stepWorth(0));
    }

    /**
     * A laptop that suspended for a minute, or a collection that stalled a pass,
     * hands in a span no robot could have driven in one stride. The run falls
     * behind rather than integrating it.
     */
    @Test
    public void aStepIsWorthNoMoreThanTheCeiling() {
        assertEquals("one minute", SimTicker.MAX_STEP_MS, SimTicker.stepWorth(60000));
        assertEquals("exactly the ceiling",
                SimTicker.MAX_STEP_MS, SimTicker.stepWorth(SimTicker.MAX_STEP_MS));
        assertEquals("one millisecond under it",
                SimTicker.MAX_STEP_MS - 1, SimTicker.stepWorth(SimTicker.MAX_STEP_MS - 1));
    }

    @Test
    public void aClockThatWentBackwardsDoesNotDriveTheRobotBackwards() {
        assertEquals("negative is no time at all", 0, SimTicker.stepWorth(-40));
    }
}
