package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L2p2: the sticks drive the wheels.
 *
 * <p>L2p1 read the sticks and wrote the numbers down. This one hands them to a
 * drivetrain. Tank drive: the left stick runs the left wheels and the right
 * stick runs the right wheels, and {@link L2TankDriveTrain#sticks} is where that
 * happens. Push both forward and the robot goes straight; push one each way and
 * it spins in place.
 *
 * <p>The loop count and the running time are gone. The robot logs both itself,
 * so writing them again was practice and not work.
 *
 * <p>Passes when: LessonsTest.l2p2_theSticksDriveTheWheelsLikeATank and
 * LessonsTest.l2p2_theSticksMoveTheSimulatedRobot
 */
@TeleOp(name = "L2p2 Tank", group = "Lessons")
public class L2p2TankOpMode extends CorbelsTeleOp {

    private L2TankDriveTrain drivetrain;

    /** What the A button was doing last loop, so a change can be spotted. */
    private boolean previousButtonA;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L2TankDriveTrain(hardware);
        initAfter();
    }

    @Override
    public void start() {
        startBefore();
        previousButtonA = false;
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();

        double leftSpeed = -gamepad1.left_stick_y;
        double rightSpeed = -gamepad1.right_stick_y;
        drivetrain.sticks(leftSpeed, rightSpeed);

        Tracker.publish("stick/leftY", leftSpeed);
        Tracker.publish("stick/leftX", gamepad1.left_stick_x);
        Tracker.publish("stick/rightY", rightSpeed);
        Tracker.publish("stick/rightX", gamepad1.right_stick_x);

        boolean buttonA = gamepad1.a;
        Tracker.publish("driver pressed A", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("lesson/event", "button A pressed");
            } else {
                Tracker.publish("lesson/event", "button A released");
            }
        }
        previousButtonA = buttonA;

        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
