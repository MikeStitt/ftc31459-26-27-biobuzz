package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.Before;
import org.junit.Test;

/**
 * One test per part the lessons add to {@link LessonsDriveTrain}: the immediate
 * write L2 fills in, the scaling L4 fills in, and the wheel sharing L6 fills in.
 *
 * <p>Two drivetrains stand in for the lessons' own. {@code Sides} is L2 through
 * L5: it drives its own drivetrain and has no {@code mix}, so the follower cannot
 * drive it. {@code Mecanum} is L6 onwards.
 */
public class LessonsDriveTrainTest {

    private static final double EPS = 1e-9;

    private OpModeHarness.FakeMotor frontLeft;
    private OpModeHarness.FakeMotor frontRight;
    private OpModeHarness.FakeMotor backLeft;
    private OpModeHarness.FakeMotor backRight;
    private RobotHardware hardware;

    /** A lesson drivetrain that owns its drivetrain, the way L2 through L5 do. */
    private static class Sides extends LessonsDriveTrain {
        Sides(RobotHardware hardware) {
            super(hardware);
        }

        void sticks(double leftSpeed, double rightSpeed) {
            driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
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
        hardware = new RobotHardware(frontLeft.motor, frontRight.motor,
                backLeft.motor, backRight.motor, new OpModeHarness.FakeImu().imu,
                OpModeHarness.freshConfig());
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
        Mecanum drivetrain = new Mecanum(hardware);
        drivetrain.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        assertTrue(drivetrain.commandedWheelsAreSet());

        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("the lesson wins while the wheels are commanded",
                new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), EPS);
    }

    @Test
    public void releasingTheWheelsHandsThemBackToTheFollower() {
        Mecanum drivetrain = new Mecanum(hardware);
        drivetrain.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        drivetrain.releaseCommandedWheels();
        assertFalse(drivetrain.commandedWheelsAreSet());

        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals(new double[]{1, 1, 1, 1}, motorPowers(), EPS);
    }

    @Test
    public void stoppingReleasesTheWheelsAndZeroesThem() {
        Mecanum drivetrain = new Mecanum(hardware);
        drivetrain.setCommandedWheels(1, 1, 1, 1);
        drivetrain.stop();
        assertFalse(drivetrain.commandedWheelsAreSet());
        assertArrayEquals(new double[]{0, 0, 0, 0}, motorPowers(), EPS);
    }

    @Test
    public void theFollowerScalesAPathTheWayPedroExpects() {
        Mecanum drivetrain = new Mecanum(hardware);
        assertEquals(1.0, drivetrain.maxScaling(DrivePowers.zero(), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.0, drivetrain.maxScaling(new DrivePowers(1, 0, 0), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.5, drivetrain.maxScaling(new DrivePowers(0.5, 0, 0), new DrivePowers(1, 0, 0)), EPS);
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

    // ------------------------------------------------------------ brake mode

    @Test
    public void theEffectiveBrakeModeIsWhatTheConfigSaysUntilCharacterizationOverridesIt() {
        Mecanum drivetrain = new Mecanum(hardware);
        assertTrue("the config asks for braking", drivetrain.getEffectiveBrakeMode());
        assertFalse(drivetrain.isCoastForCharacterization());

        drivetrain.forceCoastForCharacterization();
        assertTrue(drivetrain.isCoastForCharacterization());
        assertFalse("a measurement needs the wheels to roll", drivetrain.getEffectiveBrakeMode());

        drivetrain.allowConfiguredBrakeMode();
        assertFalse(drivetrain.isCoastForCharacterization());
        assertTrue("back to whatever the config says", drivetrain.getEffectiveBrakeMode());
    }

    @Test
    public void coastingForCharacterizationSurvivesAFollowerUpdate() {
        // The bug this replaces: L17 wrote the flag and restored it in
        // afterLoop(), which runs every loop, so the coast lasted one update.
        Mecanum drivetrain = new Mecanum(hardware);
        drivetrain.forceCoastForCharacterization();
        drivetrain.drive(new DrivePowers(0, 0, 0), true);
        drivetrain.drive(new DrivePowers(0, 0, 0), true);
        assertFalse("still coasting on the second update", drivetrain.getEffectiveBrakeMode());
    }

    @Test
    public void aConfigThatAsksForCoastingGetsItWithoutAnyOverride() {
        hardware.mecanumConfig.manualBrakeMode.set(false);
        Mecanum drivetrain = new Mecanum(hardware);
        assertFalse("nothing was overridden, and it still coasts",
                drivetrain.getEffectiveBrakeMode());
        assertFalse(drivetrain.isCoastForCharacterization());
    }

    // ------------------------------------------------------------ L11's part

    @Test
    public void fieldRelativeDrivingTurnsTheDriversViewIntoTheRobots() {
        Mecanum drivetrain = new Mecanum(hardware);

        // Facing along the field's x axis, the two views agree.
        drivetrain.fieldRelative(0, 1, 0, 0);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals("straight down the field is straight ahead",
                new double[]{1, 1, 1, 1}, motorPowers(), EPS);

        // Turned a quarter turn to the left, going down the field is strafing
        // to the robot's right, which runs one diagonal pair each way.
        drivetrain.fieldRelative(Math.PI / 2, 1, 0, 0);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals("the same journey, sideways to the robot",
                new double[]{1, -1, -1, 1}, motorPowers(), EPS);
    }

    @Test
    public void turningTheDriversViewNeverChangesHowFastTheRobotGoes() {
        // A small stick, so no wheel asks for more than full power and nothing
        // is scaled: then the four powers can be read back as a speed. A fresh
        // drivetrain each time, because these powers are small enough for the
        // write cache to swallow a step between two headings.
        for (int deg = 0; deg < 360; deg += 30) {
            Mecanum drivetrain = new Mecanum(hardware);
            drivetrain.fieldRelative(Math.toRadians(deg), 0.06, -0.08, 0);
            drivetrain.drive(DrivePowers.zero(), true);
            double[] w = motorPowers();
            double forward = (w[0] + w[1] + w[2] + w[3]) / 4;
            double strafeLeft = (-w[0] + w[1] + w[2] - w[3]) / 4;
            assertEquals("the same speed whichever way the robot faces at " + deg + " deg",
                    0.1, Math.hypot(forward, strafeLeft), 1e-9);
        }
    }

    // ------------------------------------------------------- L3a's and L3b's parts

    @Test
    public void theDeadbandIgnoresAStickThatIsNearlyCentred() {
        Mecanum drivetrain = new Mecanum(hardware);
        assertEquals("inside the band", 0.0, drivetrain.deadband(0.04, 0.05), EPS);
        assertEquals("outside it, the stick itself", 0.5, drivetrain.deadband(0.5, 0.05), EPS);
    }

    @Test
    public void squaringTheStickKeepsItsSign() {
        Mecanum drivetrain = new Mecanum(hardware);
        assertEquals(0.25, drivetrain.squared(0.5), EPS);
        assertEquals("keeps its sign", -0.25, drivetrain.squared(-0.5), EPS);
    }

    // ------------------------------------------------------------ L16's part

    @Test
    public void aWantedSpeedBecomesAFeedforwardGuessPlusACorrection() {
        Mecanum drivetrain = new Mecanum(hardware);
        // Every wheel is stopped, so the whole error is the speed asked for.
        double wanted = 10.0;
        double expected = Constants.powerPerInchPerSecond * wanted + 0.008 * wanted;

        drivetrain.setCommandedWheelSpeeds(wanted, wanted, wanted, wanted);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals(new double[]{expected, expected, expected, expected},
                motorPowers(), EPS);
    }

    @Test
    public void aWheelAlreadyAtTheWantedSpeedGetsTheGuessAndNoCorrection() {
        Mecanum drivetrain = new Mecanum(hardware);
        double wanted = 10.0;
        // getVelocity() is in ticks per second, and ticksPerInch converts it.
        frontLeft.velocity = wanted * Constants.ticksPerInch;

        drivetrain.setCommandedWheelSpeeds(wanted, wanted, wanted, wanted);
        drivetrain.drive(DrivePowers.zero(), true);
        assertEquals("no error, so no correction",
                Constants.powerPerInchPerSecond * wanted, frontLeft.power, EPS);
        assertEquals("and the stopped wheel still gets one",
                Constants.powerPerInchPerSecond * wanted + 0.008 * wanted, frontRight.power, EPS);
    }

    // ------------------------------------------------------------ L17's part

    @Test
    public void theTicksDoorReportsWhatEachEncoderCounted() {
        frontLeft.ticks = 10;
        frontRight.ticks = 20;
        backLeft.ticks = 30;
        backRight.ticks = 40;
        assertArrayEquals("front left, front right, back left, back right",
                new int[]{10, 20, 30, 40}, new Mecanum(hardware).wheelTicks());
    }

    @Test
    public void drivingNowAndLettingTheFollowerDriveTooIsRefused() {
        Mecanum drivetrain = new Mecanum(hardware);
        drivetrain.driveWheelsNow(1, 1, 1, 1);
        try {
            drivetrain.drive(new DrivePowers(1, 0, 0), true);
            fail("two writers to one motor must be refused, not silently fought over");
        } catch (IllegalStateException expected) {
            assertTrue("the message says what to do instead: " + expected.getMessage(),
                    expected.getMessage().contains("setCommandedWheels"));
        }
    }
}
