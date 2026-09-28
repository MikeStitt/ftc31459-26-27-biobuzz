package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L14: press Y and the robot drives itself to a pose and stays there.
 *
 * <p>The catch worth teaching: wheels the sticks commanded stay commanded, and
 * commanded wheels beat whatever the follower worked out. So while the command
 * is running the stick code hands the wheels back -- unless the driver moves a
 * stick, which cancels the command.
 *
 * <p>Passes when: LessonsTest.l14_pressingYDrivesToAPoseAndTheDriverCanTakeOver
 */
@TeleOp(name = "L14 Drive To Pose", group = "Lessons")
public class L14DriveToPoseOpMode extends CorbelsTeleOp {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final Pose TARGET = POSES.of(120, 72, 90);

    private boolean drivingItself;

    @Override
    protected void bindings() {
        // TODO 1: when gamepad1.y is pressed, run an instant command that
        //         calls follower.hold(TARGET) and sets drivingItself = true.
        //         (PedroCommands.hold() is instant: it sets the mode and ends.
        //         The follower stays in HOLD until something calls manual().)
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

        // TODO 2 (L14): if drivingItself and the sticks are near zero, publish
        //         Tracker.publish("drive/mode", "AUTO"), hand the wheels back with
        //         drivetrain.releaseCommandedWheels(), and return -- the follower
        //         is holding the pose and must not be argued with.
        // TODO 3 (L14): if the driver DOES move a stick, set drivingItself = false,
        //         call follower.manual(0, 0, 0) to leave HOLD -- only manual() can,
        //         and the three zeros are never used -- then publish
        //         Tracker.publish("drive/mode", "DRIVER") and drive field relative.
    }
}
