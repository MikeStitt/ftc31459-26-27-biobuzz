package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.WheelTargets;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L16: drive in real units.
 *
 * <p>Until now a stick has meant "power": push it half way and the motor gets
 * 0.5, whatever that turns out to be. The robot goes slower on a flat battery at
 * the end of a match, slower again up a ramp, and faster on blocks with its
 * wheels off the ground. Nothing in the code knows how fast the robot is going.
 *
 * <p>Here a stick means a <b>speed</b>. Full forward asks for {@link #MAX_IPS}
 * inches per second, and the robot goes that fast whether the battery is full or
 * flat -- because the code measures what the wheels are doing and corrects.
 *
 * <p>Two parts to that, and both are in
 * {@link LessonsDriveTrain#setCommandedWheelSpeeds}, which is the door this
 * lesson adds to the drivetrain:
 *
 * <ul>
 *   <li><b>Feedforward</b> -- a guess at the power needed for a wanted speed,
 *       from the fact that power and speed are roughly proportional. Gets most
 *       of the way there immediately.</li>
 *   <li><b>Feedback</b> -- a correction proportional to the error between the
 *       speed asked for and the speed measured. Cleans up what the guess got
 *       wrong: battery, friction, carpet, a ramp.</li>
 * </ul>
 *
 * <p>Feedforward alone is always a little off. Feedback alone has to build up
 * error before it does anything, so it lags. Together they are how nearly every
 * velocity controller works.
 *
 * <p>This OpMode's own job is the three lines in between: what speed the sticks
 * are asking for, and what each wheel must do for the robot to move like that.
 *
 * <p>Passes when: LessonsTest.l16_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt,
 * LessonsTest.l16_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains and
 * LessonsTest.l16_turningAskesEachSideForOppositeSpeeds
 */
@TeleOp(name = "L16 Velocity Drive", group = "Lessons")
public class L16VelocityDriveOpMode extends CorbelsTeleOp {

    /** How fast full stick asks for, forward and sideways. Inches per second. */
    private static final double MAX_IPS = 40;

    /** How fast full stick asks for in turn. Radians per second. */
    private static final double MAX_TURN_RADPS = Math.PI;

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
    public void stop() {
        // Hand the wheels back before the follower's last update, or they keep
        // whatever power the last loop commanded.
        drivetrain.stop();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        // TODO 1 (L16): read the sticks as a SPEED, not a power. Full forward asks
        //         for MAX_IPS inches per second; full turn, MAX_TURN_RADPS radians
        //         per second. Use drivetrain.deadband(value, 0.05) as usual, and
        //         remember the minus signs from L5.
        double forwardSpeedInPerS = 0;
        double strafeLeftSpeedInPerS = 0;
        double turnCcwSpeedRadPerS = 0;

        // TODO 2 (L16): work out how fast each wheel has to travel for the robot to
        //         move like that. WheelTargets.forMecanum(forward, strafeLeft,
        //         turnCcw, radius) does the arithmetic, and the radius is
        //         Constants.turnRadiusInches.
        double[] target = new double[4];

        // TODO 3 (L16): ask the drivetrain for those four speeds, with
        //         drivetrain.setCommandedWheelSpeeds(...). It measures, guesses and
        //         corrects, and publishes wheel/... for every wheel.

        Tracker.publish("command/forward_ips", forwardSpeedInPerS);
        Tracker.publish("command/left_ips", strafeLeftSpeedInPerS);
        Tracker.publish("command/turn_radps", turnCcwSpeedRadPerS);

        loopAfter();
    }
}
