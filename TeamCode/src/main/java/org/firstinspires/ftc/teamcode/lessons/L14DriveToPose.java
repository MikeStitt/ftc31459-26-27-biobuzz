package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L14: press Y and the robot drives itself to a pose and stays there.
 *
 * <p>The catch worth teaching: follower.manual() throws away whatever the
 * follower was doing. So while the command is running, the stick code must keep
 * its hands off -- unless the driver moves a stick, which cancels the command.
 */
@TeleOp(name = "L14 Drive To Pose", group = "Lessons")
public class L14DriveToPose extends CorbelsTeleOp {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose TARGET = POSES.of(120, 72, 90);

    private boolean drivingItself;

    @Override
    protected void bindings() {
        // PedroCommands.hold() is an INSTANT command: it tells the follower to
        // hold the pose and finishes straight away. The follower stays in HOLD
        // until something calls manual() -- so we track that ourselves.
        buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
            follower.hold(TARGET);
            drivingItself = true;
        }));
    }

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
    public void loop() {
        loopBefore();
        driveTheRobot();
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }

    /**
     * This lesson's own driving code. It lives in its own method because it
     * returns early, and {@code loop()} must always reach {@code loopAfter()}:
     * that is where the follower updates and the flight log is written.
     */
    private void driveTheRobot() {
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCcwSpeed = -gamepad1.right_stick_x;
        boolean driverWantsControl = Math.abs(forwardSpeed) > 0.1
                || Math.abs(strafeLeftSpeed) > 0.1 || Math.abs(turnCcwSpeed) > 0.1;

        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                return;                       // leave the follower holding
            }
            drivingItself = false;            // the driver takes over
        }
        Tracker.publish("drive/mode", "DRIVER");
        Drive.fieldRelative(follower, forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
    }
}
