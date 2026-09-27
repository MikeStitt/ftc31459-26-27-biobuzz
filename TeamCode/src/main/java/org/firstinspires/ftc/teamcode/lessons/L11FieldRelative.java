package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;

/**
 * L11: field relative. Push the stick away from you and the robot goes away
 * from you, whichever way it happens to be facing.
 *
 * <p>Passes when: LessonsTest.l11_fieldRelativeIgnoresWhichWayTheRobotFaces
 */
@TeleOp(name = "L11 Field Relative", group = "Lessons")
public class L11FieldRelative extends CorbelsTeleOp {

    private L6FollowerDriveTrain wheels;

    @Override
    public void init() {
        initBefore();
        wheels = new L6FollowerDriveTrain(hardware);
        initAfter(wheels);
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        // TODO: same three numbers as lesson 5, but through
        //       Drive.fieldRelative(...) instead of Drive.holonomic(...).
        //       Try lesson 5's version with the robot turned 180 degrees first,
        //       so you can feel the difference.
        loopAfter();
    }

    @Override
    public void stop() {
        stopAfter();
    }
}
