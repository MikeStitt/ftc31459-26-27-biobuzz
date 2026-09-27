package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Drive;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.WheelTargets;
import org.firstinspires.ftc.teamcode.base.WheelVelocities;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L16: drive in real units.
 *
 * <p>Until now a stick has meant "power": push it half way and the motor gets
 * 0.5, whatever that turns out to be. The robot goes slower on a full battery
 * at the end of a match, slower again up a ramp, and faster on blocks with its
 * wheels off the ground. Nothing in the code knows how fast the robot is going.
 *
 * <p>Here a stick means a <b>speed</b>. Full forward asks for
 * {@link Constants#maxWheelInchesPerSecond} inches per second, and the robot
 * goes that fast whether the battery is full or flat -- because the code
 * measures what the wheels are doing and corrects.
 *
 * <p>Two parts to that, and both are in {@link #loop}:
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
 * <p>Passes when: LessonsTest.l16_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt,
 * LessonsTest.l16_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains and
 * LessonsTest.l16_turningAskesEachSideForOppositeSpeeds
 */
@TeleOp(name = "L16 Velocity Drive", group = "Lessons")
public class L16VelocityDrive extends CorbelsTeleOp {

    /** How fast full stick asks for, forward and sideways. Inches per second. */
    private static final double MAX_IPS = 40;

    /** How fast full stick asks for in turn. Radians per second. */
    private static final double MAX_TURN_RADPS = Math.PI;

    /** Power per inch per second. The feedforward guess, measured by AutoTune. */
    private static final double kV = Constants.powerPerInchPerSecond;

    /** Power per inch per second of error. The feedback correction. */
    private static final double kP = 0.008;

    private L6FollowerDriveTrain wheels;
    private WheelVelocities measured;

    @Override
    public void init() {
        initBefore();
        wheels = new L6FollowerDriveTrain(hardware);
        measured = new WheelVelocities(hardware);
        initAfter(wheels);
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
        wheels.releaseCommandedWheels();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        // TODO 1: read the sticks as a SPEED, not a power. Full forward asks for
        //         MAX_IPS inches per second; full turn, MAX_TURN_RADPS radians per
        //         second. Use Drive.deadband(value, 0.05) as usual, and remember
        //         the minus signs from lesson 5.
        double forwardSpeedInPerS = 0;
        double strafeLeftSpeedInPerS = 0;
        double turnCcwSpeedRadPerS = 0;

        // TODO 2: work out how fast each wheel has to travel for the robot to move
        //         like that. WheelTargets.forMecanum(forward, strafeLeft, turnCcw, radius)
        //         does the arithmetic; the radius is Constants.turnRadiusInches.
        double[] target = new double[4];

        // TODO 3: ask the motors how fast their wheels are actually going.
        //         measured.all() hands back all four, in inches per second.
        double[] actual = new double[4];

        // TODO 4: guess a power for each wheel, then correct it by the error, and
        //         send all four with wheels.setCommandedWheels(...):
        //             power = kV * target + kP * (target - actual)
        //         clamp(...) below keeps the answer inside -1 to 1.
        double[] power = new double[4];

        String[] names = {"frontLeft", "frontRight", "backLeft", "backRight"};
        for (int i = 0; i < 4; i++) {
            Tracker.publish("wheel/" + names[i] + "/target_ips", target[i]);
            Tracker.publish("wheel/" + names[i] + "/actual_ips", actual[i]);
            Tracker.publish("wheel/" + names[i] + "/error_ips", target[i] - actual[i]);
            Tracker.publish("wheel/" + names[i] + "/power", power[i]);
        }
        Tracker.publish("command/forward_ips", forwardSpeedInPerS);
        Tracker.publish("command/left_ips", strafeLeftSpeedInPerS);
        Tracker.publish("command/turn_radps", turnCcwSpeedRadPerS);

        loopAfter();
    }

    private static double clamp(double v) {
        return Math.max(-1, Math.min(1, v));
    }
}
