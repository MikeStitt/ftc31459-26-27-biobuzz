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
        // 1. The sticks ask for a speed, not a power.
        double forwardSpeedInPerS = Drive.deadband(-gamepad1.left_stick_y, 0.05) * MAX_IPS;
        double strafeLeftSpeedInPerS = Drive.deadband(-gamepad1.left_stick_x, 0.05) * MAX_IPS;
        double turnCcwSpeedRadPerS = Drive.deadband(-gamepad1.right_stick_x, 0.05) * MAX_TURN_RADPS;

        // 2. What each wheel must do for the robot to move like that.
        double[] target = WheelTargets.forMecanum(forwardSpeedInPerS, strafeLeftSpeedInPerS,
                turnCcwSpeedRadPerS, Constants.turnRadiusInches);

        // 3. What each wheel is actually doing.
        double[] actual = measured.all();

        // 4. Guess the power, then correct it by the error.
        double[] power = new double[4];
        for (int i = 0; i < 4; i++) {
            double feedforward = kV * target[i];
            double feedback = kP * (target[i] - actual[i]);
            power[i] = clamp(feedforward + feedback);
        }
        wheels.setCommandedWheels(power[0], power[1], power[2], power[3]);

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
