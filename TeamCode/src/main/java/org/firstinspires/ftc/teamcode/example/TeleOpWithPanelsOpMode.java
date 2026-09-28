package org.firstinspires.ftc.teamcode.example;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;

/**
 * Driver-controlled OpMode, cubed sticks, nothing else.
 *
 * <p>The shortest thing a Corbels teleop can be: the four standard methods, the
 * framework's hooks around them, and one line that hands the sticks to the
 * drivetrain. Everything else -- the hardware, the follower, Panels, the flight
 * log -- comes from {@link CorbelsTeleOp}.
 *
 * <p>Cubing a stick leaves the sign alone and makes small pushes gentler, which
 * is the whole reason to do it. L3 covers the idea properly.
 *
 * <p>While this runs, open http://192.168.43.1:8001 on a device joined to the
 * robot's Wi-Fi. Loop rate, pose and velocity all plot live.
 */
@TeleOp(name = "TeleOp + Panels", group = "Corbels")
public class TeleOpWithPanelsOpMode extends CorbelsTeleOp {

    private CorbelsMecanum drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void loop() {
        loopBefore();

        double forwardSpeed = cubed(-gamepad1.left_stick_y);
        double strafeLeftSpeed = cubed(-gamepad1.left_stick_x);
        double turnCcwSpeed = cubed(-gamepad1.right_stick_x);
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);

        loopAfter();
    }

    private static double cubed(double stick) {
        return stick * stick * stick;
    }
}
