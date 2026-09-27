package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.junit.Before;
import org.junit.Test;

/**
 * One test per part the lessons add to {@link CorbelsDriveTrain}: the immediate
 * write L2 fills in, the scaling L4 fills in, and the wheel sharing L6 fills in.
 *
 * <p>Two drivetrains stand in for the lessons' own. {@code Sides} is L2 through
 * L5: it drives its own wheels and has no {@code mix}, so the follower cannot
 * drive it. {@code Mecanum} is L6 onwards.
 */
public class CorbelsDriveTrainTest {

    private static final double EPS = 1e-9;

    private OpModeHarness.FakeMotor frontLeft;
    private OpModeHarness.FakeMotor frontRight;
    private OpModeHarness.FakeMotor backLeft;
    private OpModeHarness.FakeMotor backRight;
    private RobotHardware hardware;

    /** A lesson drivetrain that owns its wheels, the way L2 through L5 do. */
    private static class Sides extends CorbelsDriveTrain {
        Sides(RobotHardware hardware) {
            super(hardware);
        }

        void sticks(double leftSpeed, double rightSpeed) {
            driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
        }

        @Override
        protected void writeWheels(double[] wheels) {
            frontLeft.setPower(wheels[0]);
            frontRight.setPower(wheels[1]);
            backLeft.setPower(wheels[2]);
            backRight.setPower(wheels[3]);
        }
    }

    /** A lesson drivetrain the follower can drive, the way L6 onwards is. */
    private static class Mecanum extends Sides {
        Mecanum(RobotHardware hardware) {
            super(hardware);
        }

        @Override
        protected double[] mix(DrivePowers powers) {
            double forwardSpeed = powers.forward();
            double strafeLeftSpeed = powers.strafe();
            double turnCcwSpeed = powers.turn();
            return new double[]{
                    forwardSpeed - strafeLeftSpeed - turnCcwSpeed,
                    forwardSpeed + strafeLeftSpeed + turnCcwSpeed,
                    forwardSpeed + strafeLeftSpeed - turnCcwSpeed,
                    forwardSpeed - strafeLeftSpeed + turnCcwSpeed};
        }
    }

    @Before
    public void setUp() {
        frontLeft = new OpModeHarness.FakeMotor();
        frontRight = new OpModeHarness.FakeMotor();
        backLeft = new OpModeHarness.FakeMotor();
        backRight = new OpModeHarness.FakeMotor();
        hardware = new RobotHardware(frontLeft.device, frontRight.device,
                backLeft.device, backRight.device, new OpModeHarness.FakeImu().device);
    }

    private double[] motorPowers() {
        return new double[]{frontLeft.power, frontRight.power, backLeft.power, backRight.power};
    }

    // ------------------------------------------------------------ L2's part

    @Test
    public void drivingTheWheelsNowReachesAllFourMotorsInOrder() {
        new Sides(hardware).driveWheelsNow(0.1, 0.2, 0.3, 0.4);
        assertArrayEquals("front left, front right, back left, back right",
                new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), EPS);
    }

    @Test
    public void tankSticksRunBothWheelsOnEachSide() {
        new Sides(hardware).sticks(1, -1);
        assertArrayEquals(new double[]{1, -1, 1, -1}, motorPowers(), EPS);
    }

    // ------------------------------------------------------------ L4's part

    @Test
    public void askingForMoreThanFullPowerScalesEveryWheelDownTogether() {
        new Sides(hardware).driveWheelsNow(2, 1, 0, -1);
        assertArrayEquals("everything divided by 2, so the ratios survive",
                new double[]{1, 0.5, 0, -0.5}, motorPowers(), EPS);
    }

    @Test
    public void powersInsideFullPowerAreLeftAlone() {
        new Sides(hardware).driveWheelsNow(0.5, 0.25, 0, -0.75);
        assertArrayEquals(new double[]{0.5, 0.25, 0, -0.75}, motorPowers(), EPS);
    }

    // ------------------------------------------------------------ L6's parts

    @Test
    public void theFollowerDrivesADrivetrainThatHasAMix() {
        new Mecanum(hardware).drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("forward runs all four the same way",
                new double[]{1, 1, 1, 1}, motorPowers(), EPS);
    }

    @Test
    public void commandedWheelsBeatWhatTheFollowerWorkedOut() {
        Mecanum wheels = new Mecanum(hardware);
        wheels.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        assertTrue(wheels.commandedWheelsAreSet());

        wheels.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("the lesson wins while the wheels are commanded",
                new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), EPS);
    }

    @Test
    public void releasingTheWheelsHandsThemBackToTheFollower() {
        Mecanum wheels = new Mecanum(hardware);
        wheels.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        wheels.releaseCommandedWheels();
        assertFalse(wheels.commandedWheelsAreSet());

        wheels.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals(new double[]{1, 1, 1, 1}, motorPowers(), EPS);
    }

    @Test
    public void stoppingReleasesTheWheelsAndZeroesThem() {
        Mecanum wheels = new Mecanum(hardware);
        wheels.setCommandedWheels(1, 1, 1, 1);
        wheels.stop();
        assertFalse(wheels.commandedWheelsAreSet());
        assertArrayEquals(new double[]{0, 0, 0, 0}, motorPowers(), EPS);
    }

    @Test
    public void theFollowerScalesAPathTheWayPedroExpects() {
        Mecanum wheels = new Mecanum(hardware);
        assertEquals(1.0, wheels.maxScaling(DrivePowers.zero(), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.0, wheels.maxScaling(new DrivePowers(1, 0, 0), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.5, wheels.maxScaling(new DrivePowers(0.5, 0, 0), new DrivePowers(1, 0, 0)), EPS);
    }

    // ------------------------------------------------------------ the two traps

    @Test
    public void aDrivetrainWithNoMixSaysSoRatherThanSittingStill() {
        try {
            new Sides(hardware).drive(new DrivePowers(1, 0, 0), true);
            fail("a drivetrain with no mix() must refuse the follower, not do nothing");
        } catch (UnsupportedOperationException expected) {
            assertTrue("the message names the class: " + expected.getMessage(),
                    expected.getMessage().contains("Sides"));
        }
    }

    @Test
    public void drivingNowAndLettingTheFollowerDriveTooIsRefused() {
        Mecanum wheels = new Mecanum(hardware);
        wheels.driveWheelsNow(1, 1, 1, 1);
        try {
            wheels.drive(new DrivePowers(1, 0, 0), true);
            fail("two writers to one motor must be refused, not silently fought over");
        } catch (IllegalStateException expected) {
            assertTrue("the message says what to do instead: " + expected.getMessage(),
                    expected.getMessage().contains("setCommandedWheels"));
        }
    }
}
