package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.pedro.Constants;
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
        assertTrue("drove forward, and got a fair way: " + pose.x(), pose.x() > 40);
        assertEquals("no sideways drift", 0, pose.y(), 1e-9);
        assertEquals("no turn", 0, pose.heading(), 1e-9);
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
        double quick = driveForward().robot.localizer.state().pose().x();
        double slow = driveForwardInSlowPasses().robot.localizer.state().pose().x();
        assertEquals("the same second covers the same ground", quick, slow, 0.01);
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
