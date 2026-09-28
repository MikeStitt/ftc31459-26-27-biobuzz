package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.RawPublisher;
import edu.wpi.first.networktables.StringPublisher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * What the simulated robot is doing, on NetworkTables, so AdvantageScope can
 * draw it.
 *
 * <p>This is a NetworkTables <b>server</b>, the part the robot plays on the
 * field. AdvantageScope and Glass are both clients: point either at
 * {@code 127.0.0.1} and it connects. Nothing here draws anything.
 *
 * <p>The names are the robot's names. {@code corbelsflightlog-pedro} writes
 * {@code /Pose}, {@code /Mode} and a {@code /vel/...} breakdown into the flight
 * log, and the same leaves appear here under {@code sim/}, so someone who has
 * learnt where to look in a robot log looks in the same place here. The sticks
 * use the names {@code L2bTankOpMode} already publishes to Panels.
 *
 * <p>The pose goes out as a WPILib {@code struct:Pose2d}, which is three
 * little-endian doubles and a schema that says so. AdvantageScope draws a bare
 * {@code double[]} too, and calls it the legacy numeric array format: it warns
 * on it in 2026 and removes it in 2027. The flight log has always written the
 * struct, so this is the same 24 bytes {@code FlightLog} writes for
 * {@code /Pose}, on a topic instead of in a file.
 *
 * <p>Passes when: SimPublisherTest
 */
public final class SimPublisher implements AutoCloseable {

    /** The NT4 port AdvantageScope looks for. */
    public static final int NT4_PORT = 5810;

    /** The NT3 port, which nothing here needs but the server opens anyway. */
    public static final int NT3_PORT = 1735;

    private final OpModeHarness harness;
    private final NetworkTableInstance nt;

    /** The struct schemas a {@code struct:Pose2d} is built out of, innermost
     *  first. The same table is {@code FlightLog.SCHEMAS}, which writes them
     *  into the flight log; it is not public, so the three a pose needs are
     *  named here. A wrong one is loud: AdvantageScope draws nothing. */
    private static final String[][] POSE_SCHEMAS = {
            {"struct:Translation2d", "double x;double y"},
            {"struct:Rotation2d", "double value"},
            {"struct:Pose2d", "Translation2d translation;Rotation2d rotation"}};

    private static final int POSE_BYTES = 3 * Double.BYTES;

    private final RawPublisher pose;
    private final ByteBuffer poseBytes =
            ByteBuffer.allocate(POSE_BYTES).order(ByteOrder.LITTLE_ENDIAN);
    private final StringPublisher mode;
    private final DoublePublisher[] wheels;
    private final DoublePublisher leftY;
    private final DoublePublisher leftX;
    private final DoublePublisher rightY;
    private final DoublePublisher rightX;
    private final DoublePublisher forwardIps;
    private final DoublePublisher strafeIps;
    private final DoublePublisher omegaRadps;

    public SimPublisher(OpModeHarness harness) {
        this(harness, NT3_PORT, NT4_PORT);
    }

    /** On other ports, for a test that must not collide with a real one. */
    public SimPublisher(OpModeHarness harness, int nt3Port, int nt4Port) {
        NtNatives.load();
        this.harness = harness;
        nt = NetworkTableInstance.create();
        nt.startServer("", "", nt3Port, nt4Port);

        for (String[] schema : POSE_SCHEMAS) {
            nt.addSchema(schema[0], "structschema", schema[1]);
        }
        pose = nt.getRawTopic("sim/Pose").publish("struct:Pose2d");
        mode = nt.getStringTopic("sim/Mode").publish();
        wheels = new DoublePublisher[]{
                nt.getDoubleTopic("sim/wheels/frontLeft").publish(),
                nt.getDoubleTopic("sim/wheels/frontRight").publish(),
                nt.getDoubleTopic("sim/wheels/backLeft").publish(),
                nt.getDoubleTopic("sim/wheels/backRight").publish()};
        leftY = nt.getDoubleTopic("sim/stick/leftY").publish();
        leftX = nt.getDoubleTopic("sim/stick/leftX").publish();
        rightY = nt.getDoubleTopic("sim/stick/rightY").publish();
        rightX = nt.getDoubleTopic("sim/stick/rightX").publish();
        forwardIps = nt.getDoubleTopic("sim/vel/forward_ips").publish();
        strafeIps = nt.getDoubleTopic("sim/vel/strafe_ips").publish();
        omegaRadps = nt.getDoubleTopic("sim/vel/omega_radps").publish();
    }

    /** The instance, for a test that wants to read its own topics back. */
    public NetworkTableInstance instance() {
        return nt;
    }

    /** One snapshot of the simulated robot. Call it once a loop. */
    public void publish() {
        MotionState state = harness.robot.localizer.state();
        Pose p = state.pose();
        pose.set(packed(FieldPose.of(p.x(), p.y(), p.heading())));
        mode.set(String.valueOf(harness.robot.follower.mode()));

        wheels[0].set(harness.motors.get(OpModeHarness.FRONT_LEFT).power);
        wheels[1].set(harness.motors.get(OpModeHarness.FRONT_RIGHT).power);
        wheels[2].set(harness.motors.get(OpModeHarness.BACK_LEFT).power);
        wheels[3].set(harness.motors.get(OpModeHarness.BACK_RIGHT).power);

        leftY.set(harness.gamepad1.left_stick_y);
        leftX.set(harness.gamepad1.left_stick_x);
        rightY.set(harness.gamepad1.right_stick_y);
        rightX.set(harness.gamepad1.right_stick_x);

        Twist twist = state.twist();
        forwardIps.set(twist.vx);
        strafeIps.set(twist.vy);
        omegaRadps.set(twist.omega);

        nt.flush();
    }

    /** {@code {x, y, headingRad}} as a {@code struct:Pose2d}'s three
     *  little-endian doubles. */
    private byte[] packed(double[] fieldPose) {
        poseBytes.clear();
        for (double value : fieldPose) {
            poseBytes.putDouble(value);
        }
        return poseBytes.array();
    }

    @Override
    public void close() {
        nt.stopServer();
        nt.close();
    }
}
