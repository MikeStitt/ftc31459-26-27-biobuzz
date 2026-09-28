package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.revhub.drivetrains.CachedMotor;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.WheelVelocities;
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
 *   <li>L2b writes {@link #writeWheels}, which sends {@link #wheelPowers} to the
 *       four motors, and {@link #driveWheelsNow}, which puts four powers there
 *       and sends them straight away.
 *   <li>L3a writes {@link #deadband} and L3b writes {@link #squared}, which
 *       reshape what the driver asked for before the wheels get it.
 *   <li>L4 writes {@link #normalized}, so asking for more than full power slows
 *       every wheel down together instead of sending the robot somewhere else.
 *   <li>L6 writes {@link #drive}, {@link #setCommandedWheels} and
 *       {@link #releaseCommandedWheels}, which is how the path follower and a
 *       lesson take turns with the wheels.
 *   <li>L11 writes {@link #fieldRelative}, which turns the driver's view of the
 *       field into the robot's view of itself.
 *   <li>L16 writes {@link #setCommandedWheelSpeeds}, the first door that takes a
 *       speed rather than a power.
 *   <li>L17a and L17b write {@link #wheelTicks}, so a measurement can ask the
 *       encoders what they counted.
 * </ul>
 *
 * <p>{@link #FL}, {@link #FR}, {@link #BL} and {@link #BR} say which wheel each
 * slot belongs to, and they are the only way to index a four-wheel array, so the
 * order is written down once instead of in every method.
 *
 * <p><b>Which way is positive.</b> Forward runs along the robot's nose, strafe
 * goes towards the robot's left, and turn goes counter-clockwise seen from
 * above. Every name here carries its direction for that reason, and a speed in
 * real units carries the unit too: {@code forwardSpeedInPerS}. The frame is
 * Pedro's, and it is drawn at
 * https://pedropathing.com/docs/pathing/reference/coordinates
 *
 * <p><b>Why we build one at all.</b> Pedro ships a complete mecanum drivetrain,
 * {@code com.pedropathing.revhub.drivetrains.Mecanum}, and in competition a team
 * may simply use it -- you do not have to be able to write what you can get from
 * an open-source library. We build ours so that the motors in a drivetrain are
 * something you understand and control, and because ours has a door Pedro's does
 * not: {@link #setCommandedWheels} lets a teleop beat the follower at the wheels.
 *
 * <p>The finished version of this class is
 * {@code org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain}, in the same
 * order with nothing left blank. Each method Pedro's {@code Drivetrain} interface
 * requires says above it where it came from.
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

    /** Power per inch per second of error, for {@link #setCommandedWheelSpeeds}. */
    private static final double VELOCITY_KP = 0.008;

    /** The robot's motors, sensors and battery. The motors are read from here. */
    protected final RobotHardware hardware;

    /**
     * The four motors in wheel order, each behind a write cache. Pedro's own
     * {@code Mecanum} wraps its motors the same way: a power that has moved by
     * less than {@code powerThreshold} does not reach the hardware, so a loop
     * that asks for the same thing twice costs one write instead of two.
     */
    private CachedMotor[] motors = new CachedMotor[4];

    /** What the wheels were last told to do. Written by whatever drives them. */
    protected final double[] wheelPowers = new double[4];

    /** Set by {@link #setCommandedWheels}, cleared by {@link #releaseCommandedWheels}. */
    private double[] commandedWheels;

    /** What {@link #normalized} last had to divide by, as Pedro reports it. */
    private double powerScale = 1.0;

    /** What the follower last asked for, for {@link #debug}. */
    private DrivePowers lastDrivePowers = DrivePowers.zero();

    /** True while {@link #forceCoastForCharacterization} is in force. */
    private boolean coastForCharacterization;

    /** How fast each wheel is actually turning, for {@link #setCommandedWheelSpeeds}. */
    private final WheelVelocities measuredSpeeds;

    /** True once a lesson has written the wheels itself. */
    private boolean drivenDirectly;

    /**
     * Takes the robot's hardware and gets the motors ready: each one spins the
     * way the config says -- the right side opposite to the left, because the two
     * sides face opposite ways on the robot -- and every wheel brakes when its
     * power goes to 0.
     */
    protected LessonsDriveTrain(RobotHardware hardware) {
        this.hardware = hardware;
        this.measuredSpeeds = new WheelVelocities(hardware);

        cacheMotors(hardware.mecanumConfig.powerThreshold.get());
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(getEffectiveBrakeMode()));
    }

    // ------------------------------------------------- L2b writes these

    /**
     * Sends {@link #wheelPowers} to the motors, each slot to the motor
     * {@link #FL} and the others name.
     */
    protected void writeWheels() {
        motors[FL].setPower(wheelPowers[FL]);
        motors[FR].setPower(wheelPowers[FR]);
        motors[BL].setPower(wheelPowers[BL]);
        motors[BR].setPower(wheelPowers[BR]);
    }

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

        wheelPowers[FL] = frontLeftPower;
        wheelPowers[FR] = frontRightPower;
        wheelPowers[BL] = backLeftPower;
        wheelPowers[BR] = backRightPower;

        normalized(wheelPowers);
        writeWheels();
    }

    // ------------------------------------------------- L4 writes this

    /**
     * Scales every power down by the same amount, if any of them asks for more
     * than full power. Dividing them all by the biggest one keeps the robot
     * going where the driver asked; chopping each one off on its own would send
     * it somewhere else.
     *
     * <p>Scales the array it is handed, hands the same array back, and records
     * what it divided by in {@link #powerScale}, which is what Pedro reports.
     */
    protected double[] normalized(double[] powers) {
        double max = 1.0;
        for (double power : powers) {
            double magnitude = Math.abs(power);
            max = Math.max(max, magnitude);
        }

        powerScale = 1.0 / max;
        for (int i = 0; i < powers.length; i++) {
            powers[i] = powers[i] / max;
        }
        return powers;
    }

    // ------------------------------------------------- L6 writes these

    /**
     * The follower's three numbers, as four wheel powers. L6 is the first lesson
     * that needs one; up to then a drivetrain drives its own wheels and the
     * follower never asks.
     *
     * <p>Refusing here is deliberate, and it is what lets L2 through L5 exist. A
     * drivetrain that drives its own wheels has no mixing to do, but the type
     * still says {@code Drivetrain}, so handing an {@code L5HolonomicDriveTrain}
     * to {@code initAfter(...)} compiles and then says this at the first follower
     * update. The promise and the refusal are the design; L6 is where the missing
     * method arrives.
     */
    protected double[] mix(DrivePowers powers) {
        throw new UnsupportedOperationException(getClass().getSimpleName()
                + " has no mix(): it drives its own wheels, so the follower cannot drive it.");
    }

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

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> Ours, and the one that
     * differs on purpose: Pedro's {@code Mecanum} sets brake mode and then mixes
     * what it was handed, and this adds the commanded-wheels branch that lets a
     * lesson beat the follower at the wheels, and the check that catches a
     * drivetrain being driven twice.
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
        lastDrivePowers = powers;

        double[] sourcePowers;
        if (commandedWheels == null) {
            double[] mixed = mix(powers);
            sourcePowers = normalized(mixed);
        } else {
            sourcePowers = commandedWheels;
        }
        for (int i = 0; i < wheelPowers.length; i++) {
            wheelPowers[i] = sourcePowers[i];
        }

        writeWheels();
    }

    // ------------------------------------------------- L3a and L3b write these, L11 the third

    /** Zero when the stick is inside the band, and the stick itself when it is not. */
    public double deadband(double value, double band) {
        if (Math.abs(value) < band) {
            return 0.0;
        } else {
            return value;
        }
    }

    /** The number times itself, with the sign it started with. */
    public double squared(double value) {
        double magnitude = value * value;
        if (value < 0.0) {
            return -magnitude;
        } else {
            return magnitude;
        }
    }

    /**
     * Drives relative to the FIELD: pushing the stick away from the driver moves
     * the robot away from the driver, whichever way it is facing.
     *
     * <p>The heading is passed in rather than read, because the drivetrain does
     * not know where the robot is -- whoever has the follower does.
     */
    public void fieldRelative(double headingRad, double fieldXSpeed, double fieldYSpeed,
                              double turnCcwSpeed) {
        double cos = Math.cos(headingRad);
        double sin = Math.sin(headingRad);
        double forwardSpeed = fieldXSpeed * cos + fieldYSpeed * sin;
        double strafeLeftSpeed = -fieldXSpeed * sin + fieldYSpeed * cos;

        double[] mixed = mix(new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed));
        double[] scaled = normalized(mixed);
        setCommandedWheels(scaled[FL], scaled[FR], scaled[BL], scaled[BR]);
    }

    // ------------------------------------------------- L16 writes this

    /**
     * Drives each wheel at the speed given, in inches per second.
     *
     * <p>Every other door here takes a power, which is whatever the battery and
     * the carpet make of it. This one takes a speed and gets it, two ways at
     * once: a feedforward guess at the power a speed needs, from
     * {@link Constants#powerPerInchPerSecond}, plus a correction proportional to
     * the difference between the speed asked for and the speed measured.
     */
    public void setCommandedWheelSpeeds(double frontLeftInPerS, double frontRightInPerS,
                                        double backLeftInPerS, double backRightInPerS) {
        double[] wanted = new double[4];
        wanted[FL] = frontLeftInPerS;
        wanted[FR] = frontRightInPerS;
        wanted[BL] = backLeftInPerS;
        wanted[BR] = backRightInPerS;

        double[] measured = measuredSpeeds.all();
        double[] powers = new double[4];
        for (int i = 0; i < powers.length; i++) {
            double feedforward = Constants.powerPerInchPerSecond * wanted[i];
            double feedback = VELOCITY_KP * (wanted[i] - measured[i]);
            powers[i] = clampToPower(feedforward + feedback);
        }

        setCommandedWheels(powers[FL], powers[FR], powers[BL], powers[BR]);
        publishWheelSpeeds(wanted, measured, powers);
    }

    // ------------------------------------------------- L17a and L17b write this

    /** What each encoder has counted, in ticks, in wheel order. */
    public int[] wheelTicks() {
        int[] ticks = new int[4];
        ticks[FL] = hardware.frontLeft.getCurrentPosition();
        ticks[FR] = hardware.frontRight.getCurrentPosition();
        ticks[BL] = hardware.backLeft.getCurrentPosition();
        ticks[BR] = hardware.backRight.getCurrentPosition();
        return ticks;
    }

    // ------------------------------------------------- brake mode

    /** Brakes or coasts as the config says, which is how a match runs. */
    public void allowConfiguredBrakeMode() {
        coastForCharacterization = false;
        cacheMotors(hardware.mecanumConfig.powerThreshold.get());
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(getEffectiveBrakeMode()));
    }

    /**
     * Gets out of the way of a measurement, until
     * {@link #allowConfiguredBrakeMode} is called: the wheels coast, so a robot
     * being pushed rolls freely, and every power reaches the hardware, so a slow
     * voltage ramp is a ramp rather than a staircase of {@code powerThreshold}
     * steps. L17a and L17b are what this is for.
     */
    public void forceCoastForCharacterization() {
        coastForCharacterization = true;
        cacheMotors(0.0);
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(getEffectiveBrakeMode()));
    }

    /** True while {@link #forceCoastForCharacterization} is in force. */
    public boolean isCoastForCharacterization() {
        return coastForCharacterization;
    }

    /** What {@link #stop()} with no argument would use: the outcome, not the override. */
    public final boolean getEffectiveBrakeMode() {
        if (coastForCharacterization) {
            return false;
        }
        return hardware.mecanumConfig.manualBrakeMode.get();
    }

    // ------------------------------------------------- the rest of what Pedro requires

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> The same algebra as
     * Pedro's {@code Mecanum}, written out: Pedro's version uses {@code lambda},
     * {@code a}, {@code b}, {@code t1}, {@code t2}, a {@code continue} and
     * {@code Utils.clamp}, and this one uses named helpers instead.
     *
     * <p>How much of {@code delta} can be added to {@code current} before a wheel
     * runs out of power. Pedro uses it so a path algorithm does not ask for more
     * than the drivetrain can give: each wheel is at some power and is being
     * asked to change by some amount, and it runs out when it reaches 1 or -1.
     * For one wheel the fraction of the change that fits is the distance to
     * whichever limit it is heading for, divided by the change; the answer for
     * the drivetrain is the smallest of those, because the first wheel to run out
     * stops the others going further.
     */
    @Override
    public double maxScaling(DrivePowers current, DrivePowers delta) {
        double[] currentPowers = mix(current);
        double[] changes = mix(delta);
        double fits = 1.0;

        for (int i = 0; i < 4; i++) {
            double power = currentPowers[i];
            double change = changes[i];

            if (movesThisWheel(change)) {
                double towardsFull = fractionThatFits(power, change, 1.0);
                double towardsFullReverse = fractionThatFits(power, change, -1.0);
                fits = smallestThatFits(fits, towardsFull);
                fits = smallestThatFits(fits, towardsFullReverse);
            }
        }

        return clampToFraction(fits);
    }

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> The same as Pedro's
     * {@code Mecanum} except for where the flag comes from: Pedro reads
     * {@code config.manualBrakeMode.get()}, and this reads
     * {@link #getEffectiveBrakeMode}, so a characterization run coasts.
     */
    @Override
    public void stop() {
        stop(getEffectiveBrakeMode());
    }

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> The same shape as Pedro's
     * {@code Mecanum}, ours in two ways: it zeroes {@link #wheelPowers} and calls
     * {@link #writeWheels} where Pedro writes each motor directly, and it clears
     * the commanded wheels, which Pedro has nothing to clear.
     */
    @Override
    public void stop(boolean brake) {
        commandedWheels = null;
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(brake));

        for (int i = 0; i < wheelPowers.length; i++) {
            wheelPowers[i] = 0.0;
        }

        writeWheels();
    }

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> Copied from Pedro's
     * {@code Mecanum} unchanged, the same one line.
     *
     * <p>It answers how fast the drivetrain can go in a direction between its
     * fast axis and its slow one.
     */
    @Override
    public double interpolateVelocity(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    /**
     * <b>Pedro's {@code Drivetrain} requires this.</b> Pedro's own eight keys,
     * spelled the way Pedro spells them, so a log from this drivetrain can be
     * read by anyone who reads Pedro's -- including the Pedro team, if we ever
     * have to send them one.
     */
    @Override
    public Map<String, Object> debug() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("forward", lastDrivePowers.forward());
        out.put("strafe", lastDrivePowers.strafe());
        out.put("turn", lastDrivePowers.turn());
        out.put("powerScale", powerScale);
        out.put("leftFrontWheelPower", wheelPowers[FL]);
        out.put("rightFrontWheelPower", wheelPowers[FR]);
        out.put("leftBackWheelPower", wheelPowers[BL]);
        out.put("rightBackWheelPower", wheelPowers[BR]);
        return out;
    }

    // ------------------------------------------------- the private parts

    /**
     * BRAKE while a driver has the sticks, so letting go stops the robot; FLOAT
     * while the follower is running a path, so its own control is not fighting
     * the wheels.
     */
    protected final void applyBrakeMode(boolean manual) {
        boolean brake = manual && getEffectiveBrakeMode();
        setZeroPowerBehavior(zeroPowerBrakeWhenTrue(brake));
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

    /** No more than full power either way. */
    private static double clampToPower(double power) {
        double notTooLow = Math.max(-1.0, power);
        return Math.min(1.0, notTooLow);
    }

    /** What a wanted speed, a measured speed and the power between them were. */
    private void publishWheelSpeeds(double[] wanted, double[] measured, double[] powers) {
        String[] names = {"frontLeft", "frontRight", "backLeft", "backRight"};
        for (int i = 0; i < names.length; i++) {
            Tracker.publish("wheel/" + names[i] + "/target_ips", wanted[i]);
            Tracker.publish("wheel/" + names[i] + "/actual_ips", measured[i]);
            Tracker.publish("wheel/" + names[i] + "/error_ips", wanted[i] - measured[i]);
            Tracker.publish("wheel/" + names[i] + "/power", powers[i]);
        }
    }

    /**
     * Puts a fresh write cache in front of each motor, with the threshold given,
     * and sets each one's direction. A new cache has forgotten what the motor was
     * last told, so the next power always reaches it.
     */
    private void cacheMotors(double powerThreshold) {
        motors = new CachedMotor[4];
        motors[FL] = new CachedMotor(hardware.frontLeft, powerThreshold);
        motors[FR] = new CachedMotor(hardware.frontRight, powerThreshold);
        motors[BL] = new CachedMotor(hardware.backLeft, powerThreshold);
        motors[BR] = new CachedMotor(hardware.backRight, powerThreshold);

        motors[FL].setDirection(hardware.mecanumConfig.frontLeftDirection.get());
        motors[FR].setDirection(hardware.mecanumConfig.frontRightDirection.get());
        motors[BL].setDirection(hardware.mecanumConfig.backLeftDirection.get());
        motors[BR].setDirection(hardware.mecanumConfig.backRightDirection.get());
    }

    /** BRAKE when true, FLOAT when false. */
    private static DcMotor.ZeroPowerBehavior zeroPowerBrakeWhenTrue(boolean given) {
        if (given) {
            return DcMotor.ZeroPowerBehavior.BRAKE;
        }
        return DcMotor.ZeroPowerBehavior.FLOAT;
    }

    private void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        for (CachedMotor motor : motors) {
            motor.setZeroPowerBehavior(behavior);
        }
    }
}
