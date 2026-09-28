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

        // TODO 1: read all four stick axes into four named doubles. Negate each
        //         y axis, so that pushing away from the driver is positive.

        // TODO 2: log all four, so Panels can draw them:
        //         Tracker.publish("stick/leftY", leftSpeed);
        //         then stick/leftX, stick/rightY and stick/rightX.

        // TODO 3: read gamepad1.a into a boolean, and log it as
        //         "driver pressed A". A boolean logs the same way a double does.

        // TODO 4: add one to loopCount, read getRuntime() into a double, and log
        //         them as "lesson/loop_count" and "lesson/seconds_running". An int
        //         and a double, so three of Java's kinds of number are now logged.

        // TODO 5: say when the button changes. If the boolean is not the same as
        //         previousButtonA, log "button A pressed" when it is now true and
        //         "button A released" when it is now false, as
        //         "lesson/event". A String logs the same way. Then remember this
        //         loop's value in previousButtonA for the next one.
        //         Works when: LessonsTest.l2a_logsEveryStickAndTheAButton and
        //         LessonsTest.l2a_saysWhenTheButtonIsPressedAndReleased pass.

        loopAfter();
    }

    @Override
    public void stop() {
        stopAfter();
    }
}
