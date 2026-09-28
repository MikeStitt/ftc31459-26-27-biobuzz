package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L3p2: half a stick is a quarter of the power.
 *
 * <p>L3p1 stopped the creep. This one changes how the rest of the travel feels:
 * squaring the number makes small pushes much gentler and leaves full push at
 * full power, which is easier to drive slowly.
 *
 * <p>{@link LessonsDriveTrain#squared} keeps the sign, because squaring a
 * negative number the ordinary way would drive the robot forwards when the
 * driver asked for backwards. It goes beside the deadband in the drivetrain,
 * where the later lessons can reach it. The deadband runs first and the squaring second, each into its own
 * variable, so what happened to a number can be read off the log.
 *
 * <p>Squaring reshapes what the driver asked for, and where it goes decides
 * which way. That is a driver's choice rather than a right answer.
 *
 * <p>Passes when: LessonsTest.l3p2_halfAStickIsAQuarterOfThePower
 */
@TeleOp(name = "L3p2 Squared", group = "Lessons")
public class L3p2SquaredOpMode extends CorbelsTeleOp {

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

        double leftDeadbanded = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightDeadbanded = drivetrain.deadband(rightRawSpeed, DEADBAND);

        double leftSpeed = drivetrain.squared(leftDeadbanded);
        double rightSpeed = drivetrain.squared(rightDeadbanded);
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
}
