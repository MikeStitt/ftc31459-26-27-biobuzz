package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The drivetrain every lesson builds on, and the lessons build it.
 *
 * <p>To start with it knows only what a drivetrain is made of: four motors,
 * which way each one spins, and that letting go of a stick should stop the
 * robot. The rest arrives one lesson at a time.
 *
 * <ul>
 *   <li>L2p2 writes {@link #driveWheelsNow}, which sends four powers to the four
 *       motors right now.
 *   <li>L4 writes {@link #normalized}, so asking for more than full power slows
 *       every wheel down together instead of sending the robot somewhere else.
 *   <li>L6 writes {@link #drive}, {@link #setCommandedWheels} and
 *       {@link #releaseCommandedWheels}, which is how the path follower and a
 *       lesson take turns with the wheels.
 * </ul>
 *
 * <p>Every lesson also builds its own drivetrain on top of this one, and the
 * method it always writes is {@link #writeWheels}, which sends {@link #wheelPowers}
 * to the four motors. {@link #FL}, {@link #FR}, {@link #BL} and {@link #BR} say
 * which wheel each slot belongs to, and they are the only way to index the array,
 * so the order is written down once instead of in every method.
 *
 * <p><b>Which way is positive.</b> Forward runs along the robot's nose, strafe
 * goes towards the robot's left, and turn goes counter-clockwise seen from
 * above. Every name here carries its direction for that reason, and a speed in
 * real units carries the unit too: {@code forwardSpeedInPerS}. The frame is
 * Pedro's, and it is drawn at
 * https://pedropathing.com/docs/pathing/reference/coordinates
 *
 * <p>The scaling in {@link #maxScaling} follows Pedro's own {@code Mecanum}
 * (BSD 3-Clause, Pedro Pathing).
 */
public abstract class LessonsDriveTrain implements Drivetrain {

    /** Front left, in {@link #wheelPowers} and in every other four-wheel array. */
    protected static final int FL = 0;

    /** Front right. */
    protected static final int FR = 1;

    /** Back left. */
    protected static final int BL = 2;

    /** Back right. */
    protected static final int BR = 3;

    /** The robot's motors, sensors and battery. The motors are read from here. */
    protected final RobotHardware hardware;

    /** The four motors in wheel order, for the settings that go to all of them. */
    private final DcMotorEx[] motors = new DcMotorEx[4];

    /** What the wheels were last told to do. Written by whatever drives them. */
    protected final double[] wheelPowers = new double[4];

    /** Set by {@link #setCommandedWheels}, cleared by {@link #releaseCommandedWheels}. */
    private double[] commandedWheels;

    /** True once a lesson has written the wheels itself. */
    private boolean drivenDirectly;

    /**
     * Takes the robot's hardware and gets the motors ready: the right side spins
     * the opposite way to the left, because the two sides face opposite ways on
     * the robot, and every wheel brakes when its power goes to 0.
     */
    protected LessonsDriveTrain(RobotHardware hardware) {
        this.hardware = hardware;

        motors[FL] = hardware.frontLeft;
        motors[FR] = hardware.frontRight;
        motors[BL] = hardware.backLeft;
        motors[BR] = hardware.backRight;

        hardware.frontLeft.setDirection(Constants.frontLeftDirection);
        hardware.frontRight.setDirection(Constants.frontRightDirection);
        hardware.backLeft.setDirection(Constants.backLeftDirection);
        hardware.backRight.setDirection(Constants.backRightDirection);

        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(Constants.manualBrakeMode));
    }

    // ------------------------------------------------- what every lesson writes

    /**
     * Sends {@link #wheelPowers} to the motors, each slot to the motor
     * {@link #FL} and the others name.
     */
    protected abstract void writeWheels();

    // ------------------------------------------------- L2p2 writes this

    /**
     * Drives each wheel at the power given, right now. Powers run from -1 to 1,
     * the way the SDK uses them.
     *
     * <p>This is the door for a lesson that owns the motors. Nothing else is
     * writing to them, so what goes in here is what the wheels do.
     */
    public final void driveWheelsNow(double frontLeftPower, double frontRightPower,
                                     double backLeftPower, double backRightPower) {
        drivenDirectly = true;

        // TODO 1 (L2p2): put each power into its own slot of wheelPowers, using
        //         FL, FR, BL and BR to say which slot is which. One line each.
        //         wheelPowers[FL] = frontLeftPower;  and so on.
        //         Then call normalized(wheelPowers), which is L4's job and does
        //         nothing yet, and writeWheels(), which your drivetrain wrote.
        //         Works when: the wheels turn the way the sticks say, and
        //         LessonsTest.l2p2_theSticksDriveTheWheelsLikeATank passes.
    }

    // ------------------------------------------------- L4 writes this

    /**
     * Scales every power down by the same amount, if any of them asks for more
     * than full power. Dividing them all by the biggest one keeps the robot
     * going where the driver asked; chopping each one off on its own would send
     * it somewhere else.
     *
     * <p>Scales the array it is handed, and hands the same array back.
     */
    protected static double[] normalized(double[] powers) {
        // TODO 2 (L4): find the biggest of the four, ignoring minus signs, or 1 if
        //         none of them reaches 1. Math.abs takes the minus sign off, and
        //         Math.max picks the bigger of two: give the magnitude its own
        //         variable. Then divide every power by that number, in a second
        //         loop, and hand the array back.
        //         Handing them back untouched is what happens now, which is why
        //         L2p2 drives fine and L4 pulls to one side at full turn.
        //         Works when: LessonsTest.l4_arcadeUsesOneStickToDriveAndOneToTurn
        //         passes, and the robot drives straight with the drive stick and
        //         the turn stick both all the way forward.
        return powers;
    }

    // ------------------------------------------------- L6 writes these

    /**
     * Drives each wheel at the power given, from the next follower update until
     * {@link #releaseCommandedWheels}.
     *
     * <p>This is the door for a lesson whose drivetrain the follower is holding.
     * While wheels are commanded this way, whatever the follower worked out is
     * ignored, so the lesson still wins.
     */
    public void setCommandedWheels(double frontLeftPower, double frontRightPower,
                                   double backLeftPower, double backRightPower) {
        // TODO 3 (L6): make a new four-slot array in commandedWheels and put each
        //         power in its own slot, the same way driveWheelsNow does.
    }

    /** Hands the wheels back to the follower. */
    public void releaseCommandedWheels() {
        // TODO 4 (L6): forget the four powers, so drive() goes back to asking mix().
        //         Setting commandedWheels to null is how a field says "nothing
        //         here".
    }

    /** True while a lesson is driving the wheels itself. */
    public boolean commandedWheelsAreSet() {
        return commandedWheels != null;
    }

    /**
     * What the follower calls every update: three numbers in, four powers out.
     * Commanded wheels win; otherwise the lesson's own {@link #mix} decides.
     *
     * <p>Final, so that brake mode cannot be missed.
     */
    @Override
    public final void drive(DrivePowers powers, boolean manual) {
        if (drivenDirectly && commandedWheels == null) {
            throw new IllegalStateException(getClass().getSimpleName()
                    + " is driven by driveWheelsNow and by the follower at the same time."
                    + " Either call initAfter() with no drivetrain, or use setCommandedWheels.");
        }
        applyBrakeMode(manual);

        // TODO 5 (L6): work out the four powers and send them on. If a lesson has
        //         not commanded the wheels, ask mix() for them and hand what comes
        //         back to normalized(), each into its own variable, then
        //         copyInto(wheelPowers, ...). If a lesson has commanded them,
        //         copyInto(wheelPowers, commandedWheels) instead. An if and an
        //         else, not a ?. Then writeWheels().
        //         Works when: L6FollowerDriveTrainTest passes, the robot drives on
        //         the sticks in L6, and L9 drives its 24 inches.
    }

    /**
     * The follower's three numbers, as four wheel powers. L6 is the first lesson
     * that needs one; up to then a drivetrain drives its own wheels and the
     * follower never asks.
     */
    protected double[] mix(DrivePowers powers) {
        throw new UnsupportedOperationException(getClass().getSimpleName()
                + " has no mix(): it drives its own wheels, so the follower cannot drive it.");
    }

    // ------------------------------------------------- already written

    /**
     * BRAKE while a driver has the sticks, so letting go stops the robot; FLOAT
     * while the follower is running a path, so its own control is not fighting
     * the wheels.
     */
    protected final void applyBrakeMode(boolean manual) {
        boolean brake = manual && Constants.manualBrakeMode;
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(brake));
    }

    /**
     * How much of {@code delta} can be added to {@code current} before a wheel
     * runs out of power.
     *
     * <p>Pedro uses this so a path algorithm does not ask for more than the
     * drivetrain can give. How it works: each wheel is already at some power and
     * is being asked to change by some amount, and a wheel runs out when it
     * reaches 1 or -1. For one wheel, the fraction of the change that fits is
     * the distance to whichever limit it is heading for, divided by the change.
     * The answer for the drivetrain is the smallest of those fractions, because
     * the first wheel to run out stops the others going further.
     *
     * <p>The maths is Pedro's, from its own {@code Mecanum}.
     */
    @Override
    public double maxScaling(DrivePowers current, DrivePowers delta) {
        double[] currentPowers = mix(current);
        double[] changes = mix(delta);
        double fits = 1.0;

        for (int wheel = 0; wheel < 4; wheel++) {
            double power = currentPowers[wheel];
            double change = changes[wheel];

            if (movesThisWheel(change)) {
                double towardsFull = fractionThatFits(power, change, 1.0);
                double towardsFullReverse = fractionThatFits(power, change, -1.0);
                fits = smallestThatFits(fits, towardsFull);
                fits = smallestThatFits(fits, towardsFullReverse);
            }
        }

        return clampToFraction(fits);
    }

    /** A change too small to matter cannot use up a wheel's power. */
    private static boolean movesThisWheel(double change) {
        return Math.abs(change) >= 1e-9;
    }

    /** How much of {@code change} fits before {@code power} reaches {@code limit}. */
    private static double fractionThatFits(double power, double change, double limit) {
        return (limit - power) / change;
    }

    /** The smaller of the two, ignoring a fraction that heads the wrong way. */
    private static double smallestThatFits(double smallest, double fraction) {
        if (fraction >= 0.0 && fraction < smallest) {
            return fraction;
        }
        return smallest;
    }

    /** No less than none of the change, and no more than all of it. */
    private static double clampToFraction(double fraction) {
        double atLeastNone = Math.max(0.0, fraction);
        return Math.min(1.0, atLeastNone);
    }

    @Override
    public void stop() {
        stop(Constants.manualBrakeMode);
    }

    @Override
    public void stop(boolean brake) {
        commandedWheels = null;
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(brake));

        for (int wheel = 0; wheel < wheelPowers.length; wheel++) {
            wheelPowers[wheel] = 0.0;
        }

        writeWheels();
    }

    @Override
    public double interpolateVelocity(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    @Override
    public Map<String, Object> debug() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("frontLeft", wheelPowers[FL]);
        out.put("frontRight", wheelPowers[FR]);
        out.put("backLeft", wheelPowers[BL]);
        out.put("backRight", wheelPowers[BR]);
        out.put("wheelsCommanded", commandedWheels != null);
        return out;
    }

    /** BRAKE when true, FLOAT when false. */
    private static DcMotor.ZeroPowerBehavior zeroPowerBrakeWhenTrue(boolean given) {
        if (given) {
            return DcMotor.ZeroPowerBehavior.BRAKE;
        }
        return DcMotor.ZeroPowerBehavior.FLOAT;
    }

    private static void copyInto(double[] destination, double[] source) {
        for (int wheel = 0; wheel < destination.length; wheel++) {
            destination[wheel] = source[wheel];
        }
    }

    private void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        for (DcMotorEx motor : motors) {
            motor.setZeroPowerBehavior(behavior);
        }
    }
}
