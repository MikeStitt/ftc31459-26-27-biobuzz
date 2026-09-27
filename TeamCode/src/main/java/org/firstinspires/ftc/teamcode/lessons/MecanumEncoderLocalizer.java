package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.base.odometry.WheelSource;

import java.util.function.LongSupplier;

/**
 * L8: work out where the robot is from the drive wheels and the IMU.
 *
 * <p>Pedro 3 ships localizers for Pinpoint and dead wheels, but not for drive
 * encoders -- so this one is yours to write. Every loop: ask how far each
 * wheel rolled since last time, turn that into "forward" and "left" for the
 * robot, rotate it by the heading, and add it to where you thought you were.
 *
 * <p>Wheel order is {frontLeft, frontRight, backLeft, backRight}, in inches.
 *
 * <p>Passes when: MecanumEncoderLocalizerTest (all of it)
 */
public class MecanumEncoderLocalizer implements Localizer {

    private final WheelSource source;
    private final LongSupplier clockNanos;

    public MecanumEncoderLocalizer(WheelSource source) {
        this(source, System::nanoTime);
    }

    /** The second form lets the tests control time instead of the clock. */
    public MecanumEncoderLocalizer(WheelSource source, LongSupplier clockNanos) {
        this.source = source;
        this.clockNanos = clockNanos;
    }

    @Override
    public void update() {
        // TODO 1 (L8): read source.wheelInches() and source.headingRadians().
        // TODO 2 (L8): on the first loop there is nothing to compare against --
        //         remember the values and return.
        // TODO 3 (L8): work out how far each wheel moved since last time, then
        //           forward = (fl + fr + bl + br) / 4
        //           left    = (-fl + fr + bl - br) / 4
        //         (Check that second one against a robot: to strafe LEFT, the
        //         front-left wheel rolls backwards.)
        // TODO 4 (L8): rotate (forward, left) by the heading and add it to x and y:
        //           x += forward * cos(h) - left * sin(h)
        //           y += forward * sin(h) + left * cos(h)
        // TODO 5 (L8): publish the new pose with MotionState.ofTwist(...).
    }

    @Override
    public void setPose(Pose pose) {
        // TODO 6 (L8): start counting from this pose. The IMU's heading can't be
        //         moved, so remember the DIFFERENCE and add it from now on.
    }

    @Override
    public MotionState state() {
        return MotionState.zero();   // TODO 5 (L8): return the pose you worked out
    }

    @Override
    public void reset() {
        // TODO 7 (L8): back to zero.
    }
}
