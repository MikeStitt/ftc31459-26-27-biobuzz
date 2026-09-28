package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.revhub.drivetrains.MecanumConfig;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import io.github.mikestitt.corbelsflightlog.ftc.FtcFlightLog;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a real OpMode on a laptop: simulated drivetrain, fake motors and a
 * fake IMU. What a lesson publishes is read back from {@link Tracker#values},
 * and what it prints from {@link #driverStation}, rather than from a file.
 */
public final class OpModeHarness {

    /** The four motor names, read from the one config every OpMode is built on. */
    public static final String FRONT_LEFT = Constants.drivetrainConfig.frontLeftName.get();
    public static final String FRONT_RIGHT = Constants.drivetrainConfig.frontRightName.get();
    public static final String BACK_LEFT = Constants.drivetrainConfig.backLeftName.get();
    public static final String BACK_RIGHT = Constants.drivetrainConfig.backRightName.get();

    /**
     * A config of this test's own, with the same values the robot's has.
     *
     * <p>Every test that needs one builds one: {@code Constants.drivetrainConfig}
     * is a static built once, so a test that wrote to it would carry the change
     * into whichever test ran next.
     */
    public static MecanumConfig freshConfig() {
        MecanumConfig robots = Constants.drivetrainConfig;
        return new MecanumConfig(c -> {
            c.frontLeftName.set(robots.frontLeftName.get());
            c.frontRightName.set(robots.frontRightName.get());
            c.backLeftName.set(robots.backLeftName.get());
            c.backRightName.set(robots.backRightName.get());
            c.frontLeftDirection.set(robots.frontLeftDirection.get());
            c.frontRightDirection.set(robots.frontRightDirection.get());
            c.backLeftDirection.set(robots.backLeftDirection.get());
            c.backRightDirection.set(robots.backRightDirection.get());
            c.manualBrakeMode.set(robots.manualBrakeMode.get());
            c.powerThreshold.set(robots.powerThreshold.get());
        });
    }

    /**
     * A motor whose encoder the test drives by hand.
     *
     * <p>Built as a {@link Proxy} rather than a class implementing DcMotorEx:
     * the real interface has dozens of methods and gains more with each SDK
     * release, and a hand-written fake would stop compiling every time. The
     * proxy answers the four calls this code makes and returns harmless
     * defaults for the rest.
     */
    public static final class FakeMotor implements InvocationHandler {
        public int ticks;
        public double power;
        public double velocity;

        /** The motor to hand to code that wants a DcMotorEx. */
        public final DcMotorEx motor = (DcMotorEx) Proxy.newProxyInstance(
                DcMotorEx.class.getClassLoader(), new Class<?>[]{DcMotorEx.class}, this);

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getCurrentPosition":
                    return ticks;
                case "getVelocity":
                    return velocity;
                case "setPower":
                    power = (Double) args[0];
                    return null;
                case "getPower":
                    return power;
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }

    /** An IMU whose heading the test sets. */
    public static final class FakeImu implements InvocationHandler {
        public double yawRadians;

        public final IMU imu = (IMU) Proxy.newProxyInstance(
                IMU.class.getClassLoader(), new Class<?>[]{IMU.class}, this);

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getRobotYawPitchRollAngles":
                    return new YawPitchRollAngles(AngleUnit.RADIANS, yawRadians, 0, 0, 0);
                case "resetYaw":
                    yawRadians = 0;
                    return null;
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }

    /** What an unstubbed call returns: zero, false, or null. */
    static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == void.class) return null;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return (char) 0;
        return null;
    }

    /**
     * The simulated clock, in nanoseconds. It moves only when {@link #loop}
     * runs, by {@link #stepMs} each time, so the motion a run integrates is the
     * same every run. Nothing here reads the wall clock.
     */
    public static final class SimClock {
        private long nanos;

        public long nanos() {
            return nanos;
        }

        public void advance(long ms) {
            nanos += ms * 1_000_000L;
        }
    }

    public final SimClock clock = new SimClock();

    /** How long one {@link #loop} takes, in simulated milliseconds. */
    public long stepMs = 5;

    public final SimRobot robot = new SimRobot(clock::nanos);
    public final Gamepad gamepad1 = new Gamepad();
    public final Gamepad gamepad2 = new Gamepad();
    public final SimRobot.DriverStation driverStation = new SimRobot.DriverStation();
    public final Map<String, FakeMotor> motors = new HashMap<>();
    public final FakeImu imu = new FakeImu();

    /** What the fake battery reports, in volts. */
    public double batteryVolts = 12.0;

    /**
     * The battery to hand to code that wants a VoltageSensor. A {@link Proxy}
     * for the same reason {@link FakeMotor} is one, and it reads
     * {@link #batteryVolts} at the moment it is asked.
     */
    public final VoltageSensor battery = (VoltageSensor) Proxy.newProxyInstance(
            VoltageSensor.class.getClassLoader(), new Class<?>[]{VoltageSensor.class},
            (proxy, method, args) -> method.getName().equals("getVoltage")
                    ? batteryVolts
                    : defaultValue(method.getReturnType()));
    /** How many times anything has resolved the robot's hardware. */
    public int lookups;

    /** Sets what each motor reports for velocity, in ticks per second. */
    public void velocities(double frontLeft, double frontRight, double backLeft, double backRight) {
        motors.get(FRONT_LEFT).velocity = frontLeft;
        motors.get(FRONT_RIGHT).velocity = frontRight;
        motors.get(BACK_LEFT).velocity = backLeft;
        motors.get(BACK_RIGHT).velocity = backRight;
    }

    /** Where this harness's flight logs go. */
    public File logFolder;

    /**
     * Every folder a harness in this JVM has logged into, oldest first. What
     * {@link SimLogs} reads to say where a failing test left its log.
     */
    static final List<File> logFolders = new ArrayList<>();

    /** The .wpilog files written so far. */
    public File[] logs() {
        File[] files = logFolder.listFiles((d, n) -> n.endsWith(".wpilog"));
        return files == null ? new File[0] : files;
    }

    private final OpMode opMode;

    /** This harness's own drivetrain config, handed to the hardware it builds. */
    public final MecanumConfig config = freshConfig();

    public OpModeHarness(OpMode opMode) {
        this.opMode = opMode;
        for (String name : new String[]{FRONT_LEFT, FRONT_RIGHT,
                BACK_LEFT, BACK_RIGHT}) {
            motors.put(name, new FakeMotor());
        }
        // No HardwareMap is built here: the real one needs an Android context.
        // The OpMode gets its devices through RobotFactory instead, which is
        // also how the follower is swapped for a simulated one.
        RobotFactory.hardware = map -> {
            lookups++;
            return new RobotHardware(
                    motors.get(FRONT_LEFT).motor,
                    motors.get(FRONT_RIGHT).motor,
                    motors.get(BACK_LEFT).motor,
                    motors.get(BACK_RIGHT).motor,
                    imu.imu,
                    battery,
                    config);
        };
        opMode.telemetry = SimRobot.telemetry(driverStation);
        opMode.gamepad1 = gamepad1;
        opMode.gamepad2 = gamepad2;
        // The simulated robot moves on what reached the motors, whoever wrote
        // it: the lesson's own code in L2 to L5, the follower through the
        // lesson's drivetrain from L6 on.
        robot.localizer.driveFrom(() -> SimRobot.fromWheels(
                motors.get(FRONT_LEFT).power,
                motors.get(FRONT_RIGHT).power,
                motors.get(BACK_LEFT).power,
                motors.get(BACK_RIGHT).power));
        // Pedro gets the simulated drivetrain, which passes what it is asked
        // for on to the drivetrain the lesson built. Without that, nothing the
        // follower computes ever reaches a motor.
        RobotFactory.follower = (map, drivetrain) -> {
            robot.drive.delegate = drivetrain;
            return robot.follower;
        };
        // Flight logs go to a temp folder, not the robot's storage or the
        // working directory. Each harness gets its own.
        try {
            logFolder = Files.createTempDirectory("corbelsflightlog-test").toFile();
            logFolder.deleteOnExit();
            logFolders.add(logFolder);
            FtcFlightLog.useDirectory(logFolder);
        } catch (IOException e) {
            throw new IllegalStateException("could not make a temp log folder", e);
        }
    }

    /** Sets all four encoders, in ticks. */
    public void setWheelTicks(int frontLeft, int frontRight, int backLeft, int backRight) {
        motors.get(FRONT_LEFT).ticks = frontLeft;
        motors.get(FRONT_RIGHT).ticks = frontRight;
        motors.get(BACK_LEFT).ticks = backLeft;
        motors.get(BACK_RIGHT).ticks = backRight;
    }

    /** The OpMode under test, for reading what it logged. */
    public OpMode opMode() {
        return opMode;
    }

    public void init() {
        opMode.init();
    }

    public void start() {
        opMode.start();
    }

    /** One loop, {@link #stepMs} of simulated time later than the last. */
    public void loop() {
        clock.advance(stepMs);
        opMode.loop();
    }

    public void loops(int count, long sleepMs) {
        for (int i = 0; i < count; i++) {
            loop();
            sleep(sleepMs);
        }
    }

    public void stop() {
        opMode.stop();
    }

    /** init, start, n loops, stop. */
    public void run(int loops, long sleepMs) {
        init();
        start();
        loops(loops, sleepMs);
        stop();
    }

    /** The powers the follower last commanded: forward, strafe, turn. */
    public double forward() {
        return robot.drive.last.forward();
    }

    public double strafe() {
        return robot.drive.last.strafe();
    }

    public double turn() {
        return robot.drive.last.turn();
    }

    public static void sleep(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void restoreFactories() {
        RobotFactory.follower = org.firstinspires.ftc.teamcode.pedro.Constants::create;
    }
}
