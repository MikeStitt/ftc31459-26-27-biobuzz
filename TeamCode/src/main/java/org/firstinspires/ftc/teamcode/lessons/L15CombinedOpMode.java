package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.HeadingHold;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L15: everything at once.
 *
 * <table>
 *   <tr><td>nothing held</td><td>field relative, holding the heading you left</td></tr>
 *   <tr><td>right bumper</td><td>robot relative</td></tr>
 *   <tr><td>A</td><td>turn to 45 degrees and hold it; the sticks still drive</td></tr>
 *   <tr><td>Y</td><td>drive to (24, 24, -45); any stick takes control back</td></tr>
 * </table>
 *
 * <p>The encoder localizer runs alongside the real one, so the field view shows
 * both answers.
 */
@TeleOp(name = "L15 Combined", group = "Lessons")
public class L15CombinedOpMode extends CorbelsTeleOp {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** Where Y drives to. */
    private static final Pose TARGET_POSE = POSES.of(24, 24, -45);

    /** Where A points. */
    private static final double TARGET_HEADING_DEGREES = 45;

    private HeadingHold heading;
    private boolean drivingItself;

    @Override
    protected void shadowLocalizers() {
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
    }

    @Override
    protected void bindings() {
        heading = new HeadingHold(Constants.foresightConfig.headingFeedback.get());

        // A: point at a fixed field heading. Translation stays with the driver.
        buttons.whenPressed(() -> gamepad1.a, Commands.instant(() -> {
            drivingItself = false;
            heading.aimAt(Math.toRadians(TARGET_HEADING_DEGREES));
        }));

        // Y: hand the whole robot to the follower until a stick moves.
        buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
            follower.hold(TARGET_POSE);
            drivingItself = true;
        }));
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
        driveTheRobot();
        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }

    /**
     * This lesson's own driving code. It lives in its own method because it
     * returns early, and {@code loop()} must always reach {@code loopAfter()}:
     * that is where the follower updates and the flight log is written.
     */
    private void driveTheRobot() {
        double forwardSpeed = drivetrain.squared(drivetrain.deadband(-gamepad1.left_stick_y, 0.05));
        double strafeLeftSpeed =
                drivetrain.squared(drivetrain.deadband(-gamepad1.left_stick_x, 0.05));
        double turnStick = drivetrain.deadband(-gamepad1.right_stick_x, 0.05);
        boolean driverWantsControl = forwardSpeed != 0 || strafeLeftSpeed != 0 || turnStick != 0;

        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                Tracker.publish("drive/target_deg", Math.toDegrees(TARGET_POSE.heading()));
                drivetrain.releaseCommandedWheels();
                return;
            }
            drivingItself = false;      // a stick moved: the driver has it back
            // Out of HOLD, which only manual() can do. The three zeros are
            // never used -- the sticks command the wheels on the next line.
            follower.manual(0, 0, 0);
            heading.release();
        }

        double turnCcwSpeed = heading.turn(follower, turnStick);
        Double held = heading.target();
        Tracker.publish("drive/aiming", held != null);
        if (held != null) Tracker.publish("drive/target_deg", Math.toDegrees(held));

        if (gamepad1.right_bumper) {
            Tracker.publish("drive/mode", "ROBOT");
            drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        } else {
            Tracker.publish("drive/mode", "FIELD");
            drivetrain.fieldRelative(follower.pose().heading(),
                    forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        }
    }
}
