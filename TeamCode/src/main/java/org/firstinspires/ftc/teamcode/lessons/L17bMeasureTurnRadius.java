package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.Calibration;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L17b: measure how far a wheel is from the middle of the robot -- by spinning
 * the robot.
 *
 * <p>Lesson 16 turned "turn at one radian per second" into a wheel speed by
 * multiplying by a radius. This measures that radius, the same way as 17a: spin
 * the robot by hand, and compare what the Pinpoint says it turned with how far
 * the drivetrain travelled.
 *
 * <p>A wheel {@code r} inches from the middle travels {@code r} inches for every
 * radian the robot turns. So radius is wheel inches divided by radians.
 *
 * <p><b>Run 17a first</b> -- this one converts ticks to inches with
 * {@link Constants#ticksPerInch}, so a wrong value there makes a wrong answer
 * here.
 *
 * <p><b>What to do.</b> Press start and spin the robot on the spot, at least
 * {@link #NEEDED_TURNS} full turns, keeping it in roughly the same place. Read
 * the number off the Driver Station.
 */
@TeleOp(name = "L17b Measure turn radius", group = "Lessons")
public class L17bMeasureTurnRadius extends CorbelsTeleOp {

    private static final double NEEDED_TURNS = 2;

    private boolean savedBrakeMode;
    private double[] startTicks;
    private double previousHeading;
    private double radians, wheelInches;

    private L6FollowerDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L6FollowerDriveTrain(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void start() {
        startBefore();
        savedBrakeMode = Constants.manualBrakeMode;
        Constants.manualBrakeMode = false;
        startTicks = ticks();
        previousHeading = follower.pose().heading();
        startAfter();
    }

    @Override
    public void stop() {
        // Hand the drivetrain back before the follower's last update, or they keep
        // whatever power the last loop commanded.
        drivetrain.stop();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        drivetrain.setCommandedWheels(0, 0, 0, 0);

        double heading = follower.pose().heading();
        radians += Calibration.unwrap(previousHeading, heading);
        previousHeading = heading;

        double[] now = ticks();
        double turnTicks = Calibration.turnPart(now[0] - startTicks[0], now[1] - startTicks[1],
                now[2] - startTicks[2], now[3] - startTicks[3]);
        wheelInches = turnTicks / Constants.ticksPerInch;

        double measured = Calibration.turnRadiusInches(wheelInches, radians);
        Tracker.publish("measure/radians", radians);
        Tracker.publish("measure/wheel_inches", wheelInches);
        Tracker.publish("measure/turnRadius_in", measured);

        Tracker.printToDs("Spin the robot  %.2f of %.0f turns",
                Math.abs(radians) / (2 * Math.PI), NEEDED_TURNS);
        Tracker.printToDs("Wheels travelled  %.1f inches", wheelInches);
        if (Math.abs(radians) >= NEEDED_TURNS * 2 * Math.PI) {
            Tracker.printToDs();
            Tracker.printToDs("turn radius  %.2f inches", measured);
            Tracker.printToDs("Measure the diagonal between drivetrain and halve it; they should agree.");
        } else {
            Tracker.printToDs("Currently  %.2f inches", measured);
        }
        Tracker.printToDs("ticksPerInch in use  %.2f", Constants.ticksPerInch);

        loopAfter();
    }

    private double[] ticks() {
        return new double[]{
                hardware.frontLeft.getCurrentPosition(), hardware.frontRight.getCurrentPosition(),
                hardware.backLeft.getCurrentPosition(), hardware.backRight.getCurrentPosition()};
    }

    @Override
    protected void afterLoop() {
        Constants.manualBrakeMode = savedBrakeMode;
    }
}
