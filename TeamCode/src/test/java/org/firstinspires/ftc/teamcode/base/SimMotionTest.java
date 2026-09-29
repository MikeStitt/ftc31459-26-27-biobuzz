package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * The simulated robot moves on what reached the motors, and it moves the same
 * way every run.
 *
 * <p>Until this, {@code SimLocalizer} integrated what Pedro commanded, and L2
 * through L5 never command Pedro anything: a viewer pointed at the simulation
 * drew a robot standing still for the first four lessons.
 *
 * <p>The teleops here are {@link SimOpModes}, not lessons, so that what is
 * being tested is the simulator and not whether a lesson is filled in.
 */
public class SimMotionTest {

    /** Where a failure left its flight log. */
    @Rule
    public final SimLogs logs = new SimLogs();

    /**
     * A teleop starts where the last OpMode stopped, and {@code stopAfter()}
     * writes that back, so a test that runs one to the end changes where the
     * next test's robot stands. Saved and restored so these stay independent.
     */
    private Pose savedStart;

    @Before
    public void saveStart() {
        savedStart = OpModeStorage.autonomousEndPose;
    }

    @After
    public void restoreStart() {
        OpModeStorage.autonomousEndPose = savedStart;
    }

    /** Where a teleop is put down, which is what these measure movement from. */
    private static Pose start() {
        return OpModeStorage.autonomousEndPose;
    }

    /** How far the robot went along the way it was facing when it started. */
    private static double forwardOf(Pose now) {
        Pose s = start();
        return (now.x() - s.x()) * Math.cos(s.heading())
                + (now.y() - s.y()) * Math.sin(s.heading());
    }

    /** How far it slid across that line, which for a tank drive is nothing. */
    private static double lateralOf(Pose now) {
        Pose s = start();
        return -(now.x() - s.x()) * Math.sin(s.heading())
                + (now.y() - s.y()) * Math.cos(s.heading());
    }

    private static double[] motorPowers(OpModeHarness h) {
        return new double[]{
                h.motors.get(OpModeHarness.FRONT_LEFT).power,
                h.motors.get(OpModeHarness.FRONT_RIGHT).power,
                h.motors.get(OpModeHarness.BACK_LEFT).power,
                h.motors.get(OpModeHarness.BACK_RIGHT).power};
    }

    /** Both sticks pushed fully forward, for one simulated second. */
    private static OpModeHarness driveForward() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;    // the stick reads negative forward
        h.gamepad1.right_stick_y = -1.0f;
        h.loops(100, 0);                    // 100 x 10 ms of simulated time
        return h;
    }

    /**
     * Without the motor powers driving the simulation, this fails with
     * {@code drove forward, and got a fair way: 0.0}. With one sign wrong in
     * the strafe row of {@link SimRobot#fromWheels}, it fails with
     * {@code no sideways drift expected:<0.0> but was:<18.53...>}.
     */
    @Test
    public void theSticksMoveTheSimulatedRobot() {
        OpModeHarness h = driveForward();
        assertArrayEquals("all four wheels at full power",
                new double[]{1, 1, 1, 1}, motorPowers(h), 1e-9);

        Pose pose = h.robot.localizer.state().pose();
        assertTrue("drove forward, and got a fair way: " + forwardOf(pose), forwardOf(pose) > 40);
        assertEquals("no sideways drift", 0, lateralOf(pose), 1e-9);
        assertEquals("no turn", start().heading(), pose.heading(), 1e-9);
        h.stop();
    }

    @Test
    public void twoRunsOfTheSameLoopsIntegrateTheSameMotion() {
        Pose first = driveForward().robot.localizer.state().pose();
        Pose again = driveForward().robot.localizer.state().pose();
        assertEquals("x", first.x(), again.x(), 0);
        assertEquals("y", first.y(), again.y(), 0);
        assertEquals("heading", first.heading(), again.heading(), 0);
    }

    /** The same simulated second, as forty passes of 25 ms rather than a hundred. */
    private static OpModeHarness driveForwardInSlowPasses() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;
        h.gamepad1.right_stick_y = -1.0f;
        for (int i = 0; i < 40; i++) {
            h.loop(25);                     // 40 x 25 ms is the same second
        }
        return h;
    }

    /**
     * How far the robot goes is what the clock was advanced by, not how many
     * times {@code loop} was called. A real run measures how long its last pass
     * took and hands that in, so a slow OpMode still moves the robot at the speed
     * it would move on the field.
     *
     * <p>The two runs differ by 0.0032 in over the second, which is the step size
     * showing: 54.7497421494739 in at 10 ms steps against 54.74657244990345 in at
     * 25 ms. The tolerance admits that and nothing near a missed step.
     */
    @Test
    public void aSlowPassCoversTheGroundItsTimeIsWorth() {
        double quick = forwardOf(driveForward().robot.localizer.state().pose());
        double slow = forwardOf(driveForwardInSlowPasses().robot.localizer.state().pose());
        assertEquals("the same second covers the same ground", quick, slow, 0.01);
    }

    /**
     * How far one step goes is what that step was worth. Recorded on 2026-09-29
     * over forty steps of 25 ms: the first covers nothing, because a step is an
     * interval and the first one has no start to measure from; the next four
     * cover 0.268 in, 0.492 in, 0.678 in and 0.834 in as the lag lets the wheels
     * take hold; and by the fortieth a step covers 1.608686 in, which is 25 ms of
     * 64.4 in/s less what is left of the lag.
     *
     * <p>With {@code loop(long)} advancing {@code stepMs} instead of what it was
     * given, the fortieth step covers 0.6003162255933034 in rather than
     * 1.608686 in, and this fails on the stride. That is not a settled step
     * either: forty steps of 10 ms is 400 ms, and the lag has not finished
     * letting go.
     */
    @Test
    public void everyStepCoversTheGroundItsOwnTimeIsWorth() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;
        h.gamepad1.right_stick_y = -1.0f;

        double[] each = new double[40];
        double last = forwardOf(h.robot.localizer.state().pose());
        for (int i = 0; i < each.length; i++) {
            h.loop(25);
            double gone = forwardOf(h.robot.localizer.state().pose());
            each[i] = gone - last;
            last = gone;
        }

        assertEquals("the first step has no interval behind it", 0, each[0], 0);
        assertTrue("and the second has one: " + each[1], each[1] > 0);
        assertEquals("a settled 25 ms step is 25 ms of 64.4 in/s",
                1.608686, each[each.length - 1], 1e-6);
        h.stop();
    }

    /**
     * Without {@code SimDrive.delegate}, this fails with all four powers at
     * {@code 0.0}, and takes L9 and L10 with it: their paths end where they
     * started, {@code expected:<96.0> but was:<72.0>}.
     */
    @Test
    public void whatTheFollowerAsksForReachesTheMotors() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Driven());
        h.init();
        h.start();
        h.robot.follower.manual(1, 0, 0);
        h.loop();
        assertArrayEquals("the follower's forward became four wheel powers",
                new double[]{1, 1, 1, 1}, motorPowers(h), 1e-9);
        h.stop();
    }
}
