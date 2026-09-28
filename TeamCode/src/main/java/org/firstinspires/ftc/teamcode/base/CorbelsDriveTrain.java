package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything a drivetrain the follower drives has to do, except the two parts a
 * lesson writes.
 *
 * <p><b>Which way is positive.</b> Pedro's three numbers are
 * {@code forwardSpeed} along the robot's nose, {@code strafeLeftSpeed} towards
 * the robot's left, and {@code turnCcwSpeed} counter-clockwise seen from above.
 * Every name in this project carries the direction for that reason, and a rate
 * in real units carries the unit too: {@code forwardSpeedInPerS},
 * {@code turnCcwSpeedRadPerS}. The frame itself is Pedro's, defined at
 * https://pedropathing.com/docs/pathing/reference/coordinates
 *
 * <p>Pedro takes any {@link Drivetrain} in its Follower constructor, so this is
 * the seam Pedro provides rather than a patch. The four motors come from
 * {@link RobotHardware}, so they are the same objects the encoder localizer
 * reads.
 *
 * <p><b>The two a lesson writes.</b> {@link #mix} turns the three numbers the
 * follower asks for -- forward, strafe and turn -- into four wheel powers.
 * {@link #writeWheels} sends those four powers to the motors. Everything else
 * here is done.
 *
 * <p><b>Who writes to the motors.</b> Only {@link #writeWheels}, and only
 * {@link #drive} and {@link #stop} call it. {@code drive} is final so that
 * brake mode cannot be missed: a subclass that replaced it would replace that
 * line too.
 *
 * <p>The saturation maths in {@link #maxScaling} follows Pedro's {@code Mecanum}
 * (BSD 3-Clause, Pedro Pathing).
 */
public abstract class CorbelsDriveTrain implements Drivetrain {

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

    private final DcMotorEx[] motors = new DcMotorEx[4];
    protected final double[] wheelPowers = new double[4];

    /** Set by {@link #setCommandedWheels}, cleared by {@link #releaseCommandedWheels}. */
    private double[] commandedWheels;

    protected CorbelsDriveTrain(RobotHardware hardware) {
        this.hardware = hardware;

        motors[FL] = hardware.frontLeft;
        motors[FR] = hardware.frontRight;
        motors[BL] = hardware.backLeft;
        motors[BR] = hardware.backRight;

        hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
        hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
        hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
        hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());
    }

    // ------------------------------------------------------- what a lesson writes

    /**
     * The three numbers the follower asks for, as four wheel powers: front left,
     * front right, back left, back right.
     */
    protected abstract double[] mix(DrivePowers powers);

    /** Sends each of the four powers to its motor. */
    protected abstract void writeWheels();

    // ------------------------------------------------------- wheel commands

    /**
     * Drives each wheel at the power given, from the next follower update until
     * {@link #releaseCommandedWheels}. Powers are -1 to 1, as the SDK uses them.
     *
     * <p>While wheels are commanded this way, whatever the follower computes is
     * ignored -- so only do it while the follower is in manual mode.
     */
    public void setCommandedWheels(double frontLeftPower, double frontRightPower,
                                   double backLeftPower, double backRightPower) {
        commandedWheels = new double[4];
        commandedWheels[FL] = frontLeftPower;
        commandedWheels[FR] = frontRightPower;
        commandedWheels[BL] = backLeftPower;
        commandedWheels[BR] = backRightPower;
    }

    /** Hands the wheels back to the follower. */
    public void releaseCommandedWheels() {
        commandedWheels = null;
    }

    /** True while a lesson is driving the wheels itself. */
    public boolean commandedWheelsAreSet() {
        return commandedWheels != null;
    }

    // ------------------------------------------------------- reading back

    /** What each wheel was last told to do: front left, front right, back left, back right. */
    public double[] wheelPowers() {
        return wheelPowers.clone();
    }

    /** What each encoder has counted, in ticks, in the same order. */
    public int[] wheelTicks() {
        int[] ticks = new int[4];
        ticks[FL] = hardware.frontLeft.getCurrentPosition();
        ticks[FR] = hardware.frontRight.getCurrentPosition();
        ticks[BL] = hardware.backLeft.getCurrentPosition();
        ticks[BR] = hardware.backRight.getCurrentPosition();
        return ticks;
    }

    // ------------------------------------------------------- Drivetrain

    @Override
    public final void drive(DrivePowers powers, boolean manual) {
        applyBrakeMode(manual);

        if (commandedWheels == null) {
            double[] mixed = mix(powers);
            double[] scaled = normalized(mixed);
            copyInto(wheelPowers, scaled);
        } else {
            copyInto(wheelPowers, commandedWheels);
        }

        writeWheels();
    }

    /**
     * BRAKE while a driver has the sticks, so letting go stops the robot; FLOAT
     * while the follower is running a path, so its own control is not fighting
     * the wheels.
     */
    protected final void applyBrakeMode(boolean manual) {
        boolean brake = manual && hardware.mecanumConfig.manualBrakeMode.get();
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(brake));
    }

    /** Scales everything down together if any wheel would exceed full power. */
    protected static double[] normalized(double[] powers) {
        double max = 1.0;
        for (double power : powers) {
            double magnitude = Math.abs(power);
            max = Math.max(max, magnitude);
        }
        for (int wheel = 0; wheel < powers.length; wheel++) {
            powers[wheel] /= max;
        }
        return powers;
    }

    /**
     * How much of {@code delta} can be added to {@code current} before a wheel
     * saturates. Pedro's algorithm uses this to avoid asking for more than the
     * drivetrain can give; the maths is Pedro's.
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
        stop(hardware.mecanumConfig.manualBrakeMode.get());
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
