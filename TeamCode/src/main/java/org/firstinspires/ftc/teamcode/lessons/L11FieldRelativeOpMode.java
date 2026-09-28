package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;

/**
 * L11: field relative. Push the stick away from you and the robot goes away
 * from you, whichever way it happens to be facing.
 *
 * <p>Passes when: LessonsTest.l11_fieldRelativeIgnoresWhichWayTheRobotFaces
 */
@TeleOp(name = "L11 Field Relative", group = "Lessons")
public class L11FieldRelativeOpMode extends CorbelsTeleOp {

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

    @Override
    public void loop() {
        loopBefore();
        // TODO (L11): the same three sticks as L5, but through
        //       drivetrain.fieldRelative(...) instead of drivetrain.sticks(...).
        //       The heading goes in first, and the drivetrain cannot get it
        //       itself: follower.pose().heading() is where it comes from.
        //       Try L5's version with the robot turned 180 degrees first, so you
        //       can feel the difference.
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
