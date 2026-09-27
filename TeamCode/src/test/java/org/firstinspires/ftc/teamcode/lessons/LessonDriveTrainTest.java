package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.junit.Before;
import org.junit.Test;

/**
 * One test per blank in {@link LessonDriveTrain}: one for each wheel of the
 * mixing, one for sending the four powers to the four motors, and one for the
 * sticks commanding the wheels while the follower holds the drivetrain. A
 * student who gets one wheel wrong sees which wheel.
 *
 * <p>These drive the class directly rather than through an OpMode, because that
 * is what the follower does: it hands over three numbers and expects four
 * powers on the motors.
 */
public class LessonDriveTrainTest {

    private static final double EPS = 1e-6;

    private OpModeHarness.FakeMotor frontLeft;
    private OpModeHarness.FakeMotor frontRight;
    private OpModeHarness.FakeMotor backLeft;
    private OpModeHarness.FakeMotor backRight;
    private LessonDriveTrain wheels;

    @Before
    public void setUp() {
        frontLeft = new OpModeHarness.FakeMotor();
        frontRight = new OpModeHarness.FakeMotor();
        backLeft = new OpModeHarness.FakeMotor();
        backRight = new OpModeHarness.FakeMotor();
        wheels = new LessonDriveTrain(new RobotHardware(
                frontLeft.device, frontRight.device, backLeft.device, backRight.device,
                new OpModeHarness.FakeImu().device));
    }

    /** What the follower does every update: three numbers in, four powers out. */
    private void follower(double forward, double left, double turn) {
        wheels.drive(new DrivePowers(forward, left, turn), true);
    }

    @Test
    public void theFrontLeftWheelIsForwardMinusLeftMinusTurn() {
        follower(1, 0, 0);
        assertEquals("driving forward runs it forward", 1.0, frontLeft.power, EPS);
        follower(0, 1, 0);
        assertEquals("sliding left runs it backwards", -1.0, frontLeft.power, EPS);
        follower(0, 0, 1);
        assertEquals("turning counter-clockwise runs it backwards", -1.0, frontLeft.power, EPS);
    }

    @Test
    public void theFrontRightWheelIsForwardPlusLeftPlusTurn() {
        follower(1, 0, 0);
        assertEquals("driving forward runs it forward", 1.0, frontRight.power, EPS);
        follower(0, 1, 0);
        assertEquals("sliding left runs it forward", 1.0, frontRight.power, EPS);
        follower(0, 0, 1);
        assertEquals("turning counter-clockwise runs it forward", 1.0, frontRight.power, EPS);
    }

    @Test
    public void theBackLeftWheelIsForwardPlusLeftMinusTurn() {
        follower(1, 0, 0);
        assertEquals("driving forward runs it forward", 1.0, backLeft.power, EPS);
        follower(0, 1, 0);
        assertEquals("sliding left runs it forward", 1.0, backLeft.power, EPS);
        follower(0, 0, 1);
        assertEquals("turning counter-clockwise runs it backwards", -1.0, backLeft.power, EPS);
    }

    @Test
    public void theBackRightWheelIsForwardMinusLeftPlusTurn() {
        follower(1, 0, 0);
        assertEquals("driving forward runs it forward", 1.0, backRight.power, EPS);
        follower(0, 1, 0);
        assertEquals("sliding left runs it backwards", -1.0, backRight.power, EPS);
        follower(0, 0, 1);
        assertEquals("turning counter-clockwise runs it forward", 1.0, backRight.power, EPS);
    }

    @Test
    public void writeWheelsSendsEachPowerToItsOwnMotor() {
        // Four different numbers, so a swapped pair cannot pass. Commanding the
        // wheels skips the mixing, which is the other blank.
        wheels.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        follower(0, 0, 0);
        assertEquals(0.1, frontLeft.power, EPS);
        assertEquals(0.2, frontRight.power, EPS);
        assertEquals(0.3, backLeft.power, EPS);
        assertEquals(0.4, backRight.power, EPS);
    }

    @Test
    public void releasingTheWheelsHandsThemBackToTheFollower() {
        wheels.setCommandedWheels(0.1, 0.2, 0.3, 0.4);
        follower(0, 0, 0);
        assertEquals("commanded, so the follower's zero is ignored", 0.1, frontLeft.power, EPS);

        wheels.releaseCommandedWheels();
        follower(0, 0, 0);
        assertEquals("released, so the follower's zero reaches the motor",
                0.0, frontLeft.power, EPS);
        assertEquals(0.0, frontRight.power, EPS);
        assertEquals(0.0, backLeft.power, EPS);
        assertEquals(0.0, backRight.power, EPS);
    }

    @Test
    public void askingForMoreThanAMotorCanGiveScalesEveryWheelDownTogether() {
        // Full forward and full left at once is a diagonal, and a mecanum drives
        // a diagonal on one pair of wheels. Unscaled the pair wants 2.
        follower(1, 1, 0);
        assertEquals("the pair that wanted 2 gets 1", 1.0, frontRight.power, EPS);
        assertEquals(1.0, backLeft.power, EPS);
        assertEquals("the other pair wanted 0 and still gets 0", 0.0, frontLeft.power, EPS);
        assertEquals(0.0, backRight.power, EPS);

        // Scaling together is what keeps the direction: halving both sides of a
        // 2-to-1 turn leaves it a 2-to-1 turn.
        follower(1, 0, 0.5);
        assertEquals(0.5 / 1.5, frontLeft.power, EPS);
        assertEquals(1.0, frontRight.power, EPS);
    }

    /**
     * L6's own move: the driver's three numbers go through the same mixing the
     * follower would have used, and the four wheels are commanded, so the
     * follower's own answer is ignored while the driver has the sticks.
     */
    @Test
    public void theSticksCommandTheWheelsSoTheDriverStillWins() {
        wheels.sticks(1, 0, 0);
        assertTrue("the wheels are commanded, not left to the follower",
                wheels.commandedWheelsAreSet());

        // Whatever the follower asks for next is ignored; the sticks decided.
        follower(0, 0, 0);
        assertEquals("driving forward runs all four forward", 1.0, frontLeft.power, EPS);
        assertEquals(1.0, frontRight.power, EPS);
        assertEquals(1.0, backLeft.power, EPS);
        assertEquals(1.0, backRight.power, EPS);
    }

    @Test
    public void theSticksAreScaledDownTogetherBeforeTheyAreCommanded() {
        // Full forward and full left at once is a diagonal: one pair wants 2.
        wheels.sticks(1, 1, 0);
        follower(0, 0, 0);
        assertEquals("the pair that wanted 2 gets 1", 1.0, frontRight.power, EPS);
        assertEquals(1.0, backLeft.power, EPS);
        assertEquals("the other pair wanted 0 and still gets 0", 0.0, frontLeft.power, EPS);
        assertEquals(0.0, backRight.power, EPS);
    }

    @Test
    public void stoppingZeroesEveryWheel() {
        follower(1, 0, 0);
        wheels.stop();
        assertEquals(0.0, frontLeft.power, EPS);
        assertEquals(0.0, frontRight.power, EPS);
        assertEquals(0.0, backLeft.power, EPS);
        assertEquals(0.0, backRight.power, EPS);
    }
}
