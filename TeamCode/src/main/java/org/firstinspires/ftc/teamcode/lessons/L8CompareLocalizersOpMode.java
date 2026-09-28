package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;

/**
 * L8: drive around while two localizers disagree.
 *
 * <p>Panels draws the follower's pose, its aim point and the path on the
 * field. Your localizer is published as Localizer/driveWheelEncoders/x_in and
 * friends, and the robot's own is Localizer/pinPoint/x_in -- so graph the two
 * against each other, then measure the robot with a tape and see which one was
 * right.
 *
 * <p>Both of them count encoders. What differs is which wheels: yours reads the
 * four that push, and the Pinpoint reads two that only measure.
 *
 * <p>Passes when: LessonsTest.l8_theEncoderLocalizerRunsAlongsideAndIsLogged
 */
@TeleOp(name = "L8 Compare Localizers", group = "Lessons")
public class L8CompareLocalizersOpMode extends CorbelsTeleOp {

    @Override
    protected void shadowLocalizers() {
        // TODO 1 (L8): run your localizer alongside the robot's own, named
        //         "driveWheelEncoders" because that is which encoders it reads:
        //         shadowLocalizers.add("driveWheelEncoders",
        //                 new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
    }

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
        // TODO 2 (L8): holonomic driving, same as L5 -- but through the drivetrain
        //         the follower is holding now, the way L6 handed it over. One call
        //         to drivetrain.sticks() with the three sticks.
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
