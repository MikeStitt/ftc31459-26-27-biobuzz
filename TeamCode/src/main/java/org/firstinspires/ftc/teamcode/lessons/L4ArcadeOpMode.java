package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L4: drivetrain drive. One stick drives, the other turns. Still no strafing.
 *
 * <p>The left stick says how fast to go. The right stick, pushed sideways, says
 * how fast to spin. {@link L4ArcadeDriveTrain#sticks} works out the four wheel
 * powers.
 *
 * <p>Pushing the right stick to the right should turn the robot to the right,
 * and turning to the right is <i>clockwise</i>. The drivetrain counts a turn as
 * positive when it is counter-clockwise, so the stick gets a minus sign. Every
 * turn number in this code is counter-clockwise-positive from here on, which is
 * also how the path follower counts.
 */
@TeleOp(name = "L4 Arcade", group = "Lessons")
public class L4ArcadeOpMode extends CorbelsTeleOp {

    private L4ArcadeDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L4ArcadeDriveTrain(hardware);
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
        double turnCcwSpeed = -gamepad1.right_stick_x;
        drivetrain.sticks(forwardSpeed, turnCcwSpeed);

        Tracker.publish("command/forward", forwardSpeed);
        Tracker.publish("command/turn_ccw", turnCcwSpeed);

        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
