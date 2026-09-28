package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L2a: read the gamepad and write down what it says. Nothing moves.
 *
 * <p>The robot can sit on the floor for this one. Every number the driver's
 * hands make goes to Panels and into the flight log, and the wheels are not
 * touched until L2b.
 *
 * <p>This is also the first look at the four methods every OpMode has.
 * {@code init} runs once when INIT is pressed, {@code start} once when PLAY is
 * pressed, {@code loop} over and over until STOP, and {@code stop} once at the
 * end. The {@code Before} and {@code After} calls are the robot's own
 * housekeeping -- hardware, the flight log, Panels -- and the code between them
 * is this lesson's.
 *
 * <p>A stick pushed away from the driver reads negative, which is backwards
 * from how anyone thinks about it, so every stick gets a minus sign on the way
 * in. After that, forward is positive.
 *
 * <p>Four kinds of value get logged here: a {@code double} for a stick, a
 * {@code boolean} for a button, an {@code int} for the loop count, and a
 * {@code String} for an event. The loop count and the running time are here to
 * show how; the robot already logs both for itself, which is worth knowing
 * before writing it a second time.
 *
 * <p>Passes when: LessonsTest.l2a_logsEveryStickAndTheAButton and
 * LessonsTest.l2a_saysWhenTheButtonIsPressedAndReleased
 */
@TeleOp(name = "L2a Sticks", group = "Lessons")
public class L2aSticksOpMode extends CorbelsTeleOp {

    /** How many times {@link #loop} has run. */
    private int loopCount;

    /** What the A button was doing last loop, so a change can be spotted. */
    private boolean previousButtonA;

    @Override
    public void init() {
        initBefore();
        initAfter();
    }

    @Override
    public void start() {
        startBefore();
        loopCount = 0;
        previousButtonA = false;
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();

        double leftSpeed = -gamepad1.left_stick_y;
        double rightSpeed = -gamepad1.right_stick_y;
        double leftSideways = gamepad1.left_stick_x;
        double rightSideways = gamepad1.right_stick_x;

        Tracker.publish("stick/leftY", leftSpeed);
        Tracker.publish("stick/leftX", leftSideways);
        Tracker.publish("stick/rightY", rightSpeed);
        Tracker.publish("stick/rightX", rightSideways);

        boolean buttonA = gamepad1.a;
        Tracker.publish("driver pressed A", buttonA);

        loopCount = loopCount + 1;
        double secondsRunning = getRuntime();
        Tracker.publish("lesson/loop_count", loopCount);
        Tracker.publish("lesson/seconds_running", secondsRunning);

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
        stopAfter();
    }
}
