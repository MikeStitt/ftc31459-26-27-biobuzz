package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;

/**
 * L11: field relative. Push the stick away from you and the robot goes away
 * from you, whichever way it happens to be facing.
 */
@TeleOp(name = "L11 Field Relative", group = "Lessons")
public class L11FieldRelative extends CorbelsTeleOp {

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
        Drive.fieldRelative(follower,
                -gamepad1.left_stick_y,      // away from the driver
                -gamepad1.left_stick_x,      // to the driver's left
                -gamepad1.right_stick_x);    // counter-clockwise
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
