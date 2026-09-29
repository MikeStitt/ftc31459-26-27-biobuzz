package org.firstinspires.ftc.teamcode.lessons;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L10: two moves in a row -- drive forward, then strafe sideways.
 *
 * <p>Both legs hold the same heading, so the robot never turns. The second leg
 * moves it sideways instead, which is what mecanum wheels are for: the robot
 * ends up 24 inches to its right, still facing the way it started.
 *
 * <p>Passes when: LessonsTest.l10_autoDrivesForwardThenStrafesSideways
 */
@Autonomous(name = "L10 Forward Then Strafe", group = "Lessons")
public class L10ForwardThenStrafeOpMode extends CorbelsAuto {

    private static final PoseFactory POSES = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    private final Pose start = POSES.of(72, botStartYIn, 90);
    private final Pose corner = POSES.of(72, 72, 90);
    private final Pose end = POSES.of(96, 72, 90);

    private L6FollowerDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L6FollowerDriveTrain(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    /**
     * An autonomous puts its driving in {@link #routine()}, and the scheduler
     * runs it from inside {@code loopAfter()}. The one line here is for the
     * driver's screen, which shows nothing unless a lesson asks it to.
     */
    @Override
    public void loop() {
        loopBefore();
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }

    @Override
    protected Pose startPose() {
        return start;
    }

    @Override
    protected Command routine() {
        return sequential(
                follow(follower, line(start, corner).constant(start)),
                follow(follower, line(corner, end).constant(start)),
                hold(follower, end)
        );
    }
}
