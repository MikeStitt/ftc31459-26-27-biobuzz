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
 *   <li>L2 writes {@link #driveWheelsNow}, which sends four powers to the four
 *       motors right now.
 *   <li>L4 writes {@link #normalized}, so asking for more than full power slows
 *       every wheel down together instead of sending the robot somewhere else.
 *   <li>L6 writes {@link #drive}, {@link #setCommandedWheels} and
 *       {@link #releaseCommandedWheels}, which is how the path follower and a
 *       lesson take turns with the wheels.
 * </ul>
 *
 * <p>Every lesson also builds its own drivetrain on top of this one, and the
 * method it always writes is {@link #writeWheels}: four powers to four motors,
 * in the order front left, front right, back left, back right. That order is the
 * same everywhere, so it is worth learning once.
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

    protected final DcMotorEx frontLeft;
    protected final DcMotorEx frontRight;
    protected final DcMotorEx backLeft;
    protected final DcMotorEx backRight;

    private final DcMotorEx[] motors;
    private final double[] wheelPowers = new double[4];

    /** Set by {@link #setCommandedWheels}, cleared by {@link #releaseCommandedWheels}. */
    private double[] commandedWheels;

    /** True once a lesson has written the wheels itself. */
    private boolean drivenDirectly;

    /**
     * Takes the four motors and gets them ready: the right side spins the
     * opposite way to the left, because the two sides face opposite ways on the
     * robot, and every wheel brakes when its power goes to 0.
     */
    protected LessonsDriveTrain(RobotHardware hardware) {
        frontLeft = hardware.frontLeft;
        frontRight = hardware.frontRight;
        backLeft = hardware.backLeft;
        backRight = hardware.backRight;
        motors = new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight};

        frontLeft.setDirection(Constants.frontLeftDirection);
        frontRight.setDirection(Constants.frontRightDirection);
        backLeft.setDirection(Constants.backLeftDirection);
        backRight.setDirection(Constants.backRightDirection);

        setZeroPowerBehavior(Constants.manualBrakeMode
                ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT);
    }

    // ------------------------------------------------- what every lesson writes

    /**
     * Sends each of the four powers to its own motor, in the order front left,
     * front right, back left, back right.
     */
    protected abstract void writeWheels(double[] wheels);

    // ------------------------------------------------- L2 writes this

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
        double[] wheels = normalized(new double[]{frontLeftPower, frontRightPower,
                backLeftPower, backRightPower});
        remember(wheels);
        writeWheels(wheels);
    }

    // ------------------------------------------------- L4 writes this

    /**
     * Scales every power down by the same amount, if any of them asks for more
     * than full power. Dividing them all by the biggest one keeps the robot
     * going where the driver asked; chopping each one off on its own would send
     * it somewhere else.
     */
    protected static double[] normalized(double[] powers) {
        double max = 1.0;
        for (double power : powers) max = Math.max(max, Math.abs(power));
        if (max == 1.0) return powers;
        for (int i = 0; i < powers.length; i++) powers[i] /= max;
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
        commandedWheels = new double[]{frontLeftPower, frontRightPower,
                backLeftPower, backRightPower};
    }

    /** Hands the wheels back to the follower. */
    public void releaseCommandedWheels() {
        commandedWheels = null;
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
        double[] wheels = commandedWheels != null
                ? commandedWheels.clone()
                : normalized(mix(powers));
        remember(wheels);
        writeWheels(wheels);
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
        setZeroPowerBehavior(manual && Constants.manualBrakeMode
                ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT);
    }

    /**
     * How much of {@code delta} can be added to {@code current} before a wheel
     * runs out of power. Pedro uses this so a path algorithm does not ask for
     * more than the drivetrain can give; the maths is Pedro's.
     */
    @Override
    public double maxScaling(DrivePowers current, DrivePowers delta) {
        double lambda = 1.0;
        double[] currentPowers = mix(current);
        double[] deltaPowers = mix(delta);
        for (int i = 0; i < 4; i++) {
            double a = currentPowers[i];
            double b = deltaPowers[i];
            if (Math.abs(b) < 1e-9) continue;
            double t1 = (1.0 - a) / b;
            double t2 = (-1.0 - a) / b;
            if (t1 >= 0.0 && t1 < lambda) lambda = t1;
            if (t2 >= 0.0 && t2 < lambda) lambda = t2;
        }
        return Math.max(0.0, Math.min(1.0, lambda));
    }

    @Override
    public void stop() {
        stop(Constants.manualBrakeMode);
    }

    @Override
    public void stop(boolean brake) {
        commandedWheels = null;
        setZeroPowerBehavior(brake ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT);
        double[] zeros = new double[4];
        remember(zeros);
        writeWheels(zeros);
    }

    @Override
    public double interpolateVelocity(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    @Override
    public Map<String, Object> debug() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("frontLeft", wheelPowers[0]);
        out.put("frontRight", wheelPowers[1]);
        out.put("backLeft", wheelPowers[2]);
        out.put("backRight", wheelPowers[3]);
        out.put("wheelsCommanded", commandedWheels != null);
        return out;
    }

    private void remember(double[] wheels) {
        System.arraycopy(wheels, 0, wheelPowers, 0, 4);
    }

    private void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        for (DcMotorEx motor : motors) motor.setZeroPowerBehavior(behavior);
    }
}
