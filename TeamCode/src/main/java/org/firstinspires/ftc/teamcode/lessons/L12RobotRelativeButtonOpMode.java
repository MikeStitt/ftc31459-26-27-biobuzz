package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;
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
        // TODO: if gamepad1.right_bumper is held, drive robot relative
        //       (Drive.holonomic); otherwise field relative.
        //       Held, not toggled -- ask a driver why.
        //       Log it too: Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
        loopAfter();
    }
}
