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
 *
 * <p>Passes when: LessonsTest.l13_theRobotHoldsItsHeadingWhenTheStickIsReleased
 */
@TeleOp(name = "L13 Heading Hold", group = "Lessons")
public class L13HeadingHoldTeleOp extends CorbelsTeleOp {

    private HeadingHold heading;

    @Override
    protected void bindings() {
        // TODO 1: build a HeadingHold from the tuned controller:
        //         new HeadingHold(Constants.foresightConfig.headingFeedback.get())
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
        // TODO 2: ask heading.turn(follower, -gamepad1.right_stick_x) for the
        //         turn power, then drive field relative with it. Log
        //         heading/holding and heading/deg so Panels can show the hold.
        loopAfter();
    }

    @Override
    public void stop() {
        stopAfter();
    }
}
