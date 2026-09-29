package org.firstinspires.ftc.teamcode.base;

/**
 * A teleop: the driver's sticks and buttons, on top of {@link CorbelsOpMode}.
 *
 * <p>A lesson writes {@code init}, {@code start}, {@code loop} and {@code stop}
 * around the framework's {@code Before} and {@code After} hooks, and may
 * override {@link #bindings()} to connect buttons to commands. Everything else
 * (the hardware, the follower, the scheduler, Panels) is handled for it.
 */
public abstract class CorbelsTeleOp extends CorbelsOpMode {

    protected Buttons buttons;

    /** Connect buttons to commands. Runs once, when the OpMode starts. */
    protected void bindings() {
    }

    @Override
    protected final boolean isAuto() { return false; }

    @Override
    protected final void onStart() {
        buttons = new Buttons();
        bindings();
        // Park the follower where it will pass the sticks through. Its own mode
        // starts IDLE, and in IDLE it stops the drivetrain every loop instead of
        // driving it.
        follower.manual(0, 0, 0);
    }

    @Override
    protected void pollInputs() {
        if (buttons != null) buttons.poll();
    }
}
