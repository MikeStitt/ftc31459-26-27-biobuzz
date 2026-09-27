package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import io.github.mikestitt.corbelsflightlog.FlightLog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * What {@link FieldPose} returns, at every quarter turn.
 *
 * <p>The same six cases as {@code FieldPoseTest} in corbelsflightlog, against
 * the copy in this repository. While two copies of the arithmetic exist, this
 * is what keeps them agreeing; when the library releases
 * {@code FlightLog.fieldPose}, both this test and {@link FieldPose} go away.
 *
 * <p>One Pedro pose, 24 inches right of the centre of the field and 12 inches
 * above it, facing 90 degrees.
 */
public class FieldPoseTest {

    private static final double X_IN = 96;
    private static final double Y_IN = 84;
    private static final double HEADING = Math.PI / 2;

    /** Those two offsets in metres. */
    private static final double RIGHT = 0.6096;
    private static final double UP = 0.3048;

    private int savedTurns;

    @Before
    public void setUp() {
        savedTurns = FlightLog.fieldQuarterTurns;
    }

    @After
    public void tearDown() {
        FlightLog.fieldQuarterTurns = savedTurns;
    }

    private static void check(int turns, double x, double y, double heading) {
        FlightLog.fieldQuarterTurns = turns;
        double[] p = FieldPose.of(X_IN, Y_IN, HEADING);
        String where = turns + " quarter turns:";
        assertEquals(where + " x", x, p[0], 1e-12);
        assertEquals(where + " y", y, p[1], 1e-12);
        assertEquals(where + " heading", heading, p[2], 1e-12);
    }

    @Test
    public void noTurnIsJustMetresFromTheCentre() {
        check(0, RIGHT, UP, HEADING);
    }

    @Test
    public void oneQuarterTurnPutsRightAboveAndUpLeft() {
        check(1, -UP, RIGHT, Math.PI);
    }

    @Test
    public void twoQuarterTurnsIsThroughTheCentre() {
        check(2, -RIGHT, -UP, 3 * Math.PI / 2);
    }

    @Test
    public void threeQuarterTurnsBringsTheHeadingBackToZero() {
        check(3, UP, -RIGHT, 0);
    }

    @Test
    public void fourQuarterTurnsIsNoTurn() {
        check(4, RIGHT, UP, HEADING);
    }

    @Test
    public void aNegativeTurnCountCountsBackwards() {
        check(-1, UP, -RIGHT, 0);
    }

    @Test
    public void theMiddleOfTheFieldIsTheOrigin() {
        FlightLog.fieldQuarterTurns = 1;
        double[] p = FieldPose.of(72, 72, 0);
        assertEquals("x", 0, p[0], 1e-12);
        assertEquals("y", 0, p[1], 1e-12);
        assertEquals("no negative zero", 0, Double.compare(p[0], 0.0));
    }
}
