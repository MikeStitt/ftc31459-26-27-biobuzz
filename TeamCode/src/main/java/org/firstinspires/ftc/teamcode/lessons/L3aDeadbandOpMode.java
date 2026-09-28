package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L3a: a stick that is nearly centred counts as centred.
 *
 * <p>A stick let go does not read exactly zero, so the robot creeps. A deadband
 * fixes it: anything smaller than {@link #DEADBAND} is treated as nothing, and
 * everything else is passed through untouched.
 *
 * <p>{@link LessonsDriveTrain#deadband} is this lesson's own work, and it is an
 * {@code if} and an {@code else} rather than anything clever. It goes in the
 * drivetrain because every later lesson shapes its sticks the same way. Both
 * sticks go through it here, so the same number is used twice.
 *
 * <p>Passes when: LessonsTest.l3a_aNearlyCentredStickCountsAsCentred
 */
@TeleOp(name = "L3a Deadband", group = "Lessons")
public class L3aDeadbandOpMode extends CorbelsTeleOp {

    /** Anything smaller than this counts as a stick that was let go. */
    // TODO 1 (L3a): pick the number. A stick let go reads a few hundredths, so
    //         0.05 is a good first guess; 0 leaves the creep exactly where it was.
    private static final double DEADBAND = 0;

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

        // TODO 2 (L3a): put each raw stick through drivetrain.deadband(), with
        //         DEADBAND as the band both times, and hand the two to
        //         drivetrain.sticks().
        double leftSpeed = 0;
        double rightSpeed = 0;

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
