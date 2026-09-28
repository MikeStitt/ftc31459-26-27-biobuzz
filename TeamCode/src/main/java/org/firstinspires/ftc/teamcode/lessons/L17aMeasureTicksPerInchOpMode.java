package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.Calibration;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L17a: measure how many encoder ticks make an inch -- by pushing the robot.
 *
 * <p>Lesson 16 needed to know how fast a wheel was turning in inches per second,
 * and the code was given a number to convert with. This measures it instead.
 *
 * <p>The trick is that the robot already knows how far it went: the Pinpoint
 * measures the floor with its own wheels, and it is already tuned. So push the
 * robot and compare two accounts of the same journey -- the Pinpoint's, in
 * inches, and the drive encoders', in ticks. The ratio is the constant.
 *
 * <p><b>What to do.</b> Press start. Push the robot slowly straight forward,
 * at least {@link #NEEDED_INCHES} inches, keeping it pointing the same way.
 * Read the number off the Driver Station and put it in
 * {@link Constants#ticksPerInch}.
 *
 * <p>Push slowly: a wheel that slips travels without turning, and the
 * measurement comes out short. Do it three times and see whether you get the
 * same answer.
 */
@TeleOp(name = "L17a Measure ticks per inch", group = "Lessons")
public class L17aMeasureTicksPerInchOpMode extends CorbelsTeleOp {

    private static final double NEEDED_INCHES = 36;

    private int[] startTicks;
    private double startX, startY;
    private double inches, ticks;

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
        // The wheels must roll freely, so no braking while we push.
        drivetrain.forceCoastForCharacterization();
        startTicks = drivetrain.wheelTicks();
        startX = follower.pose().x();
        startY = follower.pose().y();
        startAfter();
    }

    @Override
    public void stop() {
        // Braking is allowed again, however this run ended -- STOP pressed
        // early, or an exception in the loop. Then hand the wheels back before
        // the follower's last update, or they keep whatever power the last loop
        // commanded.
        drivetrain.allowConfiguredBrakeMode();
        drivetrain.stop();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        drivetrain.setCommandedWheels(0, 0, 0, 0);       // no power: we are pushing

        int[] now = drivetrain.wheelTicks();
        ticks = Calibration.forwardPart(now[0] - startTicks[0], now[1] - startTicks[1],
                now[2] - startTicks[2], now[3] - startTicks[3]);
        double dx = follower.pose().x() - startX;
        double dy = follower.pose().y() - startY;
        inches = Math.hypot(dx, dy);

        double measured = Calibration.ticksPerInch(ticks, inches);
        Tracker.publish("measure/inches", inches);
        Tracker.publish("measure/ticks", ticks);
        Tracker.publish("measure/ticksPerInch", measured);

        Tracker.printToDs("Push the robot forward  %.1f of %.0f inches", inches, NEEDED_INCHES);
        Tracker.printToDs("Encoders counted  %.0f ticks", ticks);
        if (inches >= NEEDED_INCHES) {
            Tracker.printToDs();
            Tracker.printToDs("ticksPerInch  %.2f", measured);
            Tracker.printToDs("Put that in Constants.ticksPerInch, then run it again to check.");
        } else {
            Tracker.printToDs("Currently  %.2f ticks per inch", measured);
        }
        Tracker.printToDs("Configured now  %.2f", Constants.ticksPerInch);

        loopAfter();
    }
}
