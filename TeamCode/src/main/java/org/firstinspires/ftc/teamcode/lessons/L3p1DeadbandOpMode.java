package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L3p1: a stick that is nearly centred counts as centred.
 *
 * <p>A stick let go does not read exactly zero, so the robot creeps. A deadband
 * fixes it: anything smaller than {@link #DEADBAND} is treated as nothing, and
 * everything else is passed through untouched.
 *
 * <p>{@link #deadband} is this lesson's own work, and it is an {@code if} and an
 * {@code else} rather than anything clever. Both sticks go through it, so the
 * same number is used twice.
 *
 * <p>Passes when: LessonsTest.l3p1_aNearlyCentredStickCountsAsCentred
 */
@TeleOp(name = "L3p1 Deadband", group = "Lessons")
public class L3p1DeadbandOpMode extends CorbelsTeleOp {

    /** Anything smaller than this counts as a stick that was let go. */
    private static final double DEADBAND = 0.05;

    private L2TankDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L2TankDriveTrain(hardware);
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

        double leftRawSpeed = -gamepad1.left_stick_y;
        double rightRawSpeed = -gamepad1.right_stick_y;

        double leftSpeed = deadband(leftRawSpeed, DEADBAND);
        double rightSpeed = deadband(rightRawSpeed, DEADBAND);
        drivetrain.sticks(leftSpeed, rightSpeed);

        Tracker.publish("stick/left_raw", leftRawSpeed);
        Tracker.publish("stick/left_shaped", leftSpeed);
        Tracker.publish("stick/right_raw", rightRawSpeed);
        Tracker.publish("stick/right_shaped", rightSpeed);

        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }

    /** Zero when the stick is inside the band, and the stick itself when it is not. */
    private static double deadband(double value, double band) {
        if (Math.abs(value) < band) {
            return 0.0;
        } else {
            return value;
        }
    }
}
