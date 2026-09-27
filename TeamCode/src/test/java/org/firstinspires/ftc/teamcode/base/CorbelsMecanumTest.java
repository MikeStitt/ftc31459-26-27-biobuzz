package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.drivetrain.DrivePowers;

import org.junit.Before;
import org.junit.Test;

/**
 * Our drivetrain, in place of Pedro's. It has to behave exactly as Pedro's did
 * for the follower, and additionally let a lesson drive the wheels itself.
 */
public class CorbelsMecanumTest {

    private OpModeHarness h;
    private CorbelsMecanum drivetrain;

    /** A teleop that does nothing, just to get a harness and its fake motors. */
    public static class Idle extends CorbelsTeleOp {
        CorbelsMecanum drivetrain;

        @Override public void init() {
            initBefore();
            drivetrain = new CorbelsMecanum(hardware);
            initAfter(drivetrain);
        }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    @Before
    public void setUp() {
        h = new OpModeHarness(new Idle());
        h.init();
        drivetrain = ((Idle) h.opMode()).drivetrain;
    }

    private double[] motorPowers() {
        return new double[]{
                h.motors.get(org.firstinspires.ftc.teamcode.pedro.Constants.frontLeftName).power,
                h.motors.get(org.firstinspires.ftc.teamcode.pedro.Constants.frontRightName).power,
                h.motors.get(org.firstinspires.ftc.teamcode.pedro.Constants.backLeftName).power,
                h.motors.get(org.firstinspires.ftc.teamcode.pedro.Constants.backRightName).power};
    }

    @Test
    public void forwardDrivesAllFourWheelsTheSameWay() {
        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals(new double[]{1, 1, 1, 1}, motorPowers(), 1e-9);
    }

    @Test
    public void strafingDrivesTheDiagonalsOppositeWays() {
        drivetrain.drive(new DrivePowers(0, 1, 0), true);
        assertArrayEquals("front left and back right go one way, the others the other",
                new double[]{-1, 1, 1, -1}, motorPowers(), 1e-9);
    }

    @Test
    public void turningDrivesTheSidesOppositeWays() {
        drivetrain.drive(new DrivePowers(0, 0, 1), true);
        assertArrayEquals(new double[]{-1, 1, -1, 1}, motorPowers(), 1e-9);
    }

    @Test
    public void everythingAtOnceIsScaledDownTogetherRatherThanClipped() {
        drivetrain.drive(new DrivePowers(1, 1, 1), true);
        double[] powers = motorPowers();
        for (double p : powers) assertTrue("no wheel over full power: " + p, Math.abs(p) <= 1.0 + 1e-9);
        // forward + strafe + turn would be 3 on one wheel; everything divides by 3
        assertEquals(1.0, powers[1], 1e-9);
        assertEquals(-1.0 / 3, powers[0], 1e-9);
    }

    @Test
    public void aLessonCanDriveTheWheelsItselfAndHandThemBack() {
        drivetrain.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        assertTrue(drivetrain.commandedWheelsAreSet());

        // What the follower asks for is ignored while a lesson is driving.
        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals(new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), 1e-9);

        drivetrain.releaseCommandedWheels();
        assertFalse(drivetrain.commandedWheelsAreSet());
        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("the follower has them back", new double[]{1, 1, 1, 1}, motorPowers(), 1e-9);
    }

    @Test
    public void stoppingReleasesTheWheelsAndZeroesThem() {
        drivetrain.setCommandedWheels(1, 1, 1, 1);
        drivetrain.stop();
        assertFalse(drivetrain.commandedWheelsAreSet());
        assertArrayEquals(new double[]{0, 0, 0, 0}, motorPowers(), 1e-9);
    }

    /**
     * The lesson that forgets. {@code Idle} never calls
     * {@link CorbelsDriveTrain#releaseCommandedWheels}, so the only thing that can
     * let go of the wheels when the OpMode ends is {@code stopAfter}.
     */
    @Test
    public void endingTheOpModeStopsTheWheelsEvenIfTheLessonForgot() {
        h.start();
        drivetrain.setCommandedWheels(1, 1, 1, 1);
        // What the follower does on every update, and what puts power on a motor.
        drivetrain.drive(new DrivePowers(0, 0, 0), true);
        assertArrayEquals("running, at the last power the lesson commanded",
                new double[]{1, 1, 1, 1}, motorPowers(), 1e-9);

        h.stop();
        assertFalse("the wheels are handed back", drivetrain.commandedWheelsAreSet());
        assertArrayEquals("and every wheel is at 0", new double[]{0, 0, 0, 0}, motorPowers(), 1e-9);
    }

    @Test
    public void maxScalingMatchesWhatPedroExpects() {
        // From a standstill, a full-forward delta can be applied entirely.
        assertEquals(1.0, drivetrain.maxScaling(DrivePowers.zero(), new DrivePowers(1, 0, 0)), 1e-9);
        // Already at full forward, none of another full-forward delta fits.
        assertEquals(0.0, drivetrain.maxScaling(new DrivePowers(1, 0, 0), new DrivePowers(1, 0, 0)), 1e-9);
        // Half way there, half of it fits.
        assertEquals(0.5, drivetrain.maxScaling(new DrivePowers(0.5, 0, 0), new DrivePowers(1, 0, 0)), 1e-9);
    }

    @Test
    public void wheelTargetsAreSpeedsNotPowers() {
        // Forward only: every wheel travels at the robot's speed.
        assertArrayEquals(new double[]{20, 20, 20, 20},
                WheelTargets.forMecanum(20, 0, 0, 8), 1e-9);
        // Turning only: each side goes opposite, at omega times the radius.
        assertArrayEquals(new double[]{-8, 8, -8, 8},
                WheelTargets.forMecanum(0, 0, 1.0, 8), 1e-9);
        // Left only: the diagonals split. Front left runs backwards, front
        // right forwards -- a mecanum travelling to its own left.
        assertArrayEquals(new double[]{-10, 10, 10, -10},
                WheelTargets.forMecanum(0, 10, 0, 8), 1e-9);
    }
}
