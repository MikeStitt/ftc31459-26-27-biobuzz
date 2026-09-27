package org.firstinspires.ftc.teamcode.example;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Minimal autonomous: drive 24 inches forward, holding heading. Built with Ivy so
 * adding a second step later is one more line in the sequential() block.
 *
 * <p>Pedro's coordinate frame: +x is to the right of the field and heading 0
 * faces +x. Starting at (0, 0, 0) and ending at (24, 0, 0) is therefore
 * "24 inches in whatever direction the robot is pointing at start".
 *
 * <p>NOTE ON HEADING: this uses {@code .constant()}, not the {@code .linear()}
 * the Pedro docs show. Open issues #176 and #181 both report .linear()
 * interpolating heading BACKWARDS on line paths in Pedro 3.0.x. Since this path
 * does not change heading at all, constant interpolation is both correct and
 * avoids that code path. Do not "fix" this to .linear().
 */
@Autonomous(name = "Auto: Drive 24in", group = "Corbels")
public class DriveForward24OpMode extends CorbelsAuto {

    /** How far to drive, in inches. */
    private static final double DISTANCE_IN = 24.0;

    private final PoseFactory poses = PoseFactory.degrees();
    private final Pose start = poses.of(0, 0, 0);
    private final Pose end = poses.of(DISTANCE_IN, 0, 0);

    @Override
    public void init() {
        initBefore();
        initAfter(new CorbelsMecanum(hardware));
    }

    @Override
    protected Pose startPose() {
        return start;
    }

    @Override
    protected Command routine() {
        return sequential(
                follow(follower, driveForward())
                // Add more steps here; each runs after the previous finishes.
        );
    }

    @Override
    public void loop() {
        loopBefore();

        Tracker.printToDs("Mode  %s", follower.mode());
        Tracker.printToDs("Remaining  %.1f in", DISTANCE_IN - follower.pose().x());
        Tracker.printPoseSpeedLoopToDs(follower);

        loopAfter();
    }

    private Path driveForward() {
        return line(start, end).constant(start);
    }
}
