package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L5: drivetrain drive. Now the robot can strafe.
 *
 * <p>Arcade used two numbers, forward and turn, and both sides of the robot did
 * the same thing. Mecanum wheels can do a third: slide sideways without turning.
 * That needs all four wheels mixed separately, which is what
 * {@link L5HolonomicDriveTrain#sticks} does.
 *
 * <p>The left stick now does two jobs at once -- push it to drive, push it
 * sideways to slide -- and the right stick still turns. Everything is relative
 * to the robot's own front: push the stick left and the robot slides towards its
 * own left, whichever way it happens to be facing. Making it slide towards the
 * driver's left instead comes in L11.
 */
@TeleOp(name = "L5 Holonomic", group = "Lessons")
public class L5Holonomic extends CorbelsTeleOp {

    private L5HolonomicDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L5HolonomicDriveTrain(hardware);
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
