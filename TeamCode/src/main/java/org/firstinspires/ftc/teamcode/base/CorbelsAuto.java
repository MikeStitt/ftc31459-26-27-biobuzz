package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

/**
 * An autonomous: a starting pose and a routine, on top of {@link CorbelsOpMode}.
 *
 * <p>A lesson supplies {@link #startPose()} -- where the robot is placed -- and
 * {@link #routine()}, the commands to run.
 */
public abstract class CorbelsAuto extends CorbelsOpMode {

    @Override
    protected final boolean isAuto() { return true; }

    /** Where the robot is placed before the match. */
    protected abstract Pose startPose();

    /** What the robot should do. */
    protected abstract Command routine();

    @Override
    protected final void onInit() {
        follower.setPose(startPose());
    }

    @Override
    protected final void onStart() {
        Scheduler.schedule(routine());
    }
}
