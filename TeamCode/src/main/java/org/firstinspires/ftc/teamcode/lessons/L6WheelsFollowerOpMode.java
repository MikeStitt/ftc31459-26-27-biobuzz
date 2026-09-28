package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L6: let the path follower hold the drivetrain.
 *
 * <p>L5 drove exactly as well as this does, and the sticks feel the same. What
 * changes is who is holding the drivetrain. Up to now the follower was given a
 * stand-in that ignored it, so the lesson had the motors to itself. Here the
 * drivetrain goes to {@code initAfter}, and the follower has it.
 *
 * <p>That is worth doing because the follower can do more than pass the sticks
 * through. It knows where the robot is, it can hold a heading, and it can drive
 * a path. None of that is possible while the follower is holding a stand-in, and
 * all of it is what L8 onwards is about.
 *
 * <p>Handing the drivetrain over does not hand the driver's sticks over.
 * {@link L6FollowerDriveTrain#sticks} commands the four drivetrain, and a commanded
 * wheel beats whatever the follower worked out, so the driver still wins. L8 is
 * where the drivetrain go back.
 *
 * <p>Two things to notice. {@code init} passes the drivetrain to
 * {@code initAfter}, which is how the follower gets it. And {@code stop} hands
 * the wheels back before the last follower update, or they keep whatever the
 * last loop commanded.
 */
@TeleOp(name = "L6 Follower Wheels", group = "Lessons")
public class L6WheelsFollowerOpMode extends CorbelsTeleOp {

    // TODO 1: L5 made an L5HolonomicDriveTrain. This lesson needs an
    //         L6FollowerDriveTrain instead, here and on the line below that
    //         builds it.
    private L5HolonomicDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L5HolonomicDriveTrain(hardware);
        // TODO 2: hand the drivetrain to initAfter(), so the follower holds
        //         it. L2 through L5 called initAfter() with nothing, because
        //         they drove their own wheels.
        //         Works when: L6FollowerDriveTrainTest passes and the robot
        //         drives on the sticks.
        initAfter();
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();

        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCcwSpeed = -gamepad1.right_stick_x;
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);

        Tracker.publish("command/forward", forwardSpeed);
        Tracker.publish("command/left", strafeLeftSpeed);
        Tracker.publish("command/turn_ccw", turnCcwSpeed);

        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
