package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.controllers.Controller;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.pedropathing.math.Vector2D;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * A real Pedro {@link Follower} driving a simulated robot, for tests.
 *
 * <p>The localizer turns forward, strafe and turn into motion (power x max
 * speed, with a short lag standing in for inertia) and integrates a pose.
 * Crude physics, but enough for the follower to finish a path the way it does
 * on the robot.
 *
 * <p>Where those three numbers come from is the caller's choice. On its own
 * this class uses what Pedro last commanded, which is all a follower test
 * needs. {@code OpModeHarness} points it at the four motor powers instead, so
 * a lesson that writes the motors itself moves too -- see
 * {@link #fromWheels}.
 */
public final class SimRobot {

    public static final double MAX_FORWARD_IPS = 64.4;
    public static final double MAX_STRAFE_IPS = 43.6;
    public static final double MAX_TURN_RADPS = 4.0;
    public static final double LAG_S = 0.15;

    public final SimDrive drive = new SimDrive();
    public final SimLocalizer localizer;
    public final Follower follower;

    /** Real time, which is what a test that does not hold a clock wants. */
    public SimRobot() {
        this(System::nanoTime);
    }

    /**
     * A robot whose motion is integrated against {@code clock} rather than the
     * wall clock, in nanoseconds. Two runs of the same loops then integrate the
     * same motion.
     */
    public SimRobot(LongSupplier clock) {
        localizer = new SimLocalizer(drive, clock);
        follower = new Follower(localizer, drive, new Foresight(config()));
    }

    /**
     * Four wheel powers back into forward, strafe and turn: the exact inverse
     * of {@code CorbelsMecanum.mix}, which writes
     * {@code fl = f - s - t, fr = f + s + t, bl = f + s - t, br = f - s + t}.
     * Substituting those four here returns f, s and t unchanged.
     *
     * <p>Wheel powers, not wheel speeds, because the motion model's input is a
     * power. A wheel at positive power drives the robot forward: the motor's
     * own direction is set from {@code Constants} and is taken to be right.
     */
    public static double[] fromWheels(double frontLeft, double frontRight,
                                      double backLeft, double backRight) {
        return new double[]{
                (frontLeft + frontRight + backLeft + backRight) / 4,
                (-frontLeft + frontRight + backLeft - backRight) / 4,
                (-frontLeft + frontRight - backLeft + backRight) / 4};
    }

    public static final PoseFactory POSES = PoseFactory.degrees();

    /**
     * Foresight settings for the simulation. A copy of values tuned on the
     * robot in September 2026; retuning the robot doesn't require changing
     * these -- they only need to let the follower finish a path.
     */
    public static ForesightConfig config() {
        return new ForesightConfig(c -> {
            Controller forwardP = Controller.proportional(0.368944499316663);
            Controller forwardS = Controller.proportional(0.13631513411892088);
            Controller strafeP = Controller.proportional(0.28643896916380074);
            Controller strafeS = Controller.proportional(0.10583154531580646);
            c.forwardTranslational.set(Controller.piecewise(forwardS).put(2.5, forwardP));
            c.strafeTranslational.set(Controller.piecewise(strafeS).put(2.5, strafeP));
            c.coast.set(Controller.proportionalFeedforward(0.016695978563625216));
            c.brake.set(Controller.proportionalFeedforward(0.014191581779081433));
            c.headingFeedback.set(Controller.proportional(4.443284774829611));
            c.headingBrakeCoefficients.set(Vector2D.cartesian(0.20442551239984175, -0.004974765042140602));
            c.linearBrakeCoefficients.set(Matrix.diag(0.04993877265618792, 0.08334033043867983));
            c.quadraticBrakeCoefficients.set(Matrix.diag(0.0011536551239685066, 6.430819946483606E-4));
            c.maxAchievableForwardVelocity.set(MAX_FORWARD_IPS);
            c.maxAchievableStrafeVelocity.set(MAX_STRAFE_IPS);
            c.naturalForwardDeceleration.set(46.50217822427653);
            c.naturalStrafeDeceleration.set(62.02176070971554);
        });
    }

    /**
     * The drivetrain Pedro is given. It records what Pedro asked for and hands
     * the same request on to {@link #delegate}, the drivetrain the lesson
     * built, so the powers reach the motors the way they do on the robot.
     */
    public static final class SimDrive implements Drivetrain {
        volatile DrivePowers last = DrivePowers.zero();

        /** The lesson's own drivetrain, or null when nothing is behind this. */
        public volatile Drivetrain delegate;

        @Override
        public void drive(DrivePowers powers, boolean manual) {
            last = powers;
            if (delegate != null) delegate.drive(powers, manual);
        }

        @Override
        public double maxScaling(DrivePowers a, DrivePowers b) {
            return 1;
        }

        @Override
        public void stop() {
            last = DrivePowers.zero();
            if (delegate != null) delegate.stop();
        }

        @Override
        public void stop(boolean brake) {
            last = DrivePowers.zero();
            if (delegate != null) delegate.stop(brake);
        }

        @Override
        public double interpolateVelocity(double xRatio, double yRatio, double t) {
            return MAX_FORWARD_IPS;
        }

        @Override
        public Map<String, Object> debug() {
            return new HashMap<>();
        }
    }

    public static final class SimLocalizer implements Localizer {
        private final LongSupplier clock;
        private Supplier<double[]> chassisVelocitySupplier;
        private double x;
        private double y;
        private double heading;
        private double vx;
        private double vy;
        private double omega;
        private long lastNs;
        private MotionState state = MotionState.zero();

        SimLocalizer(SimDrive drive, LongSupplier clock) {
            this.clock = clock;
            this.chassisVelocitySupplier = () -> new double[]{
                    drive.last.forward(), drive.last.strafe(), drive.last.turn()};
        }

        /**
         * Where forward, strafe and turn come from. Pass the four motor powers
         * through {@link SimRobot#fromWheels} to drive the simulation from what
         * the code actually wrote to the wheels.
         */
        public void driveFrom(Supplier<double[]> chassisVelocitySupplier) {
            this.chassisVelocitySupplier = chassisVelocitySupplier;
        }

        @Override
        public void setPose(Pose p) {
            x = p.x();
            y = p.y();
            heading = p.heading();
            state = state.withPose(p);
        }

        @Override
        public MotionState state() {
            return state;
        }

        @Override
        public void reset() {
            x = y = heading = vx = vy = omega = 0;
            state = MotionState.zero();
        }

        @Override
        public void update() {
            long now = clock.getAsLong();
            double dt = lastNs == 0 ? 0 : (now - lastNs) / 1e9;
            lastNs = now;
            double k = Math.min(1, dt / LAG_S);
            double[] c = chassisVelocitySupplier.get();
            vx += (c[0] * MAX_FORWARD_IPS - vx) * k;
            vy += (c[1] * MAX_STRAFE_IPS - vy) * k;
            omega += (c[2] * MAX_TURN_RADPS - omega) * k;
            x += (vx * Math.cos(heading) - vy * Math.sin(heading)) * dt;
            y += (vx * Math.sin(heading) + vy * Math.cos(heading)) * dt;
            heading += omega * dt;
            state = MotionState.ofTwist(POSES.of(x, y, Math.toDegrees(heading)), new Twist(vx, vy, omega));
        }
    }

    /**
     * What the Driver Station shows, and how it loses what it is not shown.
     *
     * <p>FTC's {@code Telemetry} defaults to {@code setAutoClear(true)}, so
     * {@code update()} transmits the buffer and empties it. A second
     * {@code update()} in the same loop therefore transmits nothing and throws
     * away what the first one sent, which is why {@code Tracker} owns the only
     * flush. This fake behaves the same way, so a test can watch it happen.
     */
    public static final class DriverStation {
        private final List<String> pending = new ArrayList<>();
        private List<String> shown = new ArrayList<>();

        void add(String line) {
            pending.add(line);
        }

        void transmit() {
            shown = new ArrayList<>(pending);
            pending.clear();
        }

        void clearPending() {
            pending.clear();
        }

        /** The lines the driver can see now, from the last transmission. */
        public List<String> lines() {
            return shown;
        }

        /** Those lines as one string, for a readable assertion. */
        public String text() {
            return String.join("\n", shown);
        }

        public boolean shows(String fragment) {
            return text().contains(fragment);
        }
    }

    /**
     * A Driver Station {@link Telemetry} writing into a {@link DriverStation}.
     * A dynamic proxy rather than a class, so SDK versions that add methods to
     * the interface don't break the tests.
     */
    public static Telemetry telemetry(DriverStation ds) {
        return (Telemetry) Proxy.newProxyInstance(Telemetry.class.getClassLoader(),
                new Class<?>[]{Telemetry.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("addLine")) {
                        ds.add(args == null || args.length == 0 ? "" : String.valueOf(args[0]));
                    } else if (name.equals("addData") && args != null && args.length >= 2) {
                        String value;
                        if (args.length == 3 && args[1] instanceof String) {
                            value = String.format((String) args[1], (Object[]) args[2]);
                        } else {
                            value = String.valueOf(args[1]);
                        }
                        ds.add(args[0] + ": " + value);
                    } else if (name.equals("update")) {
                        ds.transmit();
                    } else if (name.equals("clear") || name.equals("clearAll")) {
                        ds.clearPending();
                    }
                    Class<?> r = method.getReturnType();
                    if (r == boolean.class) return true;
                    if (r == int.class) return 0;
                    if (r == long.class) return 0L;
                    if (r == double.class) return 0.0;
                    if (r == float.class) return 0f;
                    return null;
                });
    }

}
