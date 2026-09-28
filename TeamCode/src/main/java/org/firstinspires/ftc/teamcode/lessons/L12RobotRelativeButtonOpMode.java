package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L12: field relative normally, robot relative while the right bumper is held
 * -- which is what a driver wants when lining up against a wall.
 *
 * <p>Passes when: LessonsTest.l12_theBumperSwitchesToRobotRelative
 */
@TeleOp(name = "L12 Robot Relative Button", group = "Lessons")
public class L12RobotRelativeButtonOpMode extends CorbelsTeleOp {

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
        startAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCcwSpeed = -gamepad1.right_stick_x;
        // TODO (L12): if gamepad1.right_bumper is held, drive robot relative
        //       (drivetrain.sticks); otherwise field relative
        //       (drivetrain.fieldRelative, with follower.pose().heading() first).
        //       Held, not toggled -- ask a driver why.
        //       Log it too: Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
        loopAfter();
    }
}
