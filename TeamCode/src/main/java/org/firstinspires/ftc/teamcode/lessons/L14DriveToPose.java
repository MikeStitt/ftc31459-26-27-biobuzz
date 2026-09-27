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
 *
 * <p>Passes when: LessonsTest.l14_pressingYDrivesToAPoseAndTheDriverCanTakeOver
 */
@TeleOp(name = "L14 Drive To Pose", group = "Lessons")
public class L14DriveToPose extends CorbelsTeleOp {

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
        driveTheRobot();
        loopAfter();
    }

    @Override
    public void stop() {
        stopAfter();
    }

    /**
     * This lesson's own driving code. It lives in its own method because it
     * returns early, and {@code loop()} must always reach {@code loopAfter()}:
     * that is where the follower updates and the flight log is written.
     */
    private void driveTheRobot() {
        // TODO 2: if drivingItself and the sticks are near zero, call
        //         Tracker.publish("drive/mode", "AUTO") and return without calling
        //         any Drive method -- let the follower hold.
        // TODO 3: if the driver DOES move a stick, set drivingItself = false, call
        //         Tracker.publish("drive/mode", "DRIVER"), and drive field relative.
    }
}
