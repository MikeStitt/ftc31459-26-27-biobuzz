package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;
import org.firstinspires.ftc.teamcode.base.HeadingHold;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L13: stop the drift. When the turn stick is released the robot holds the
 * heading it was left at, using the SAME controller the autonomous uses.
 */
@TeleOp(name = "L13 Heading Hold", group = "Lessons")
public class L13HeadingHoldTeleOp extends CorbelsTeleOp {

    private HeadingHold heading;

    @Override
    protected void bindings() {
        heading = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
    }

    private L6FollowerDriveTrain wheels;

    @Override
    public void init() {
        initBefore();
        wheels = new L6FollowerDriveTrain(hardware);
        initAfter(wheels);
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        double turnCcwSpeed = heading.turn(follower, -gamepad1.right_stick_x);
        Drive.fieldRelative(follower,
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, turnCcwSpeed);
        Tracker.publish("heading/holding", heading.target() != null);
        Tracker.publish("heading/deg", Math.toDegrees(follower.pose().heading()));
        loopAfter();
    }

    @Override
    public void stop() {
        stopAfter();
    }
}
