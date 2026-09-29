package org.firstinspires.ftc.teamcode.lessons;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L9: the first autonomous -- drive 24 inches forward, and stop there.
 *
 * <p>Passes when: LessonsTest.l9_autoDrives24InchesForwardAndStops
 */
@Autonomous(name = "L9 Drive 24", group = "Lessons")
public class L9Drive24OpMode extends CorbelsAuto {

    private static final PoseFactory POSES = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    private final Pose start = POSES.of(72, botStartYIn, 90);
    private final Pose end = POSES.of(72, botStartYIn+24, 90);


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
        // TODO: return a sequence with one step: follow a straight line from
        //       start to end, holding the heading it starts at.
        //           return sequential(follow(follower, line(start, end).constant(start)));
        //       .constant(pose) takes the heading from a pose. The number form,
        //       .constant(0), is radians, so .constant(90) is not 90 degrees.
        //       Use .constant(), not .linear() -- Pedro 3.0.1 issues #176 and #181.
        return Command.NOOP;
    }
}
