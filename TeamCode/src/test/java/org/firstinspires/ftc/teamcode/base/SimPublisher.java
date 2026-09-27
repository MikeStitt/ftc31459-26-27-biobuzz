package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;

import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;

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
 * use the names {@code L2Sticks} already publishes to Panels.
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

    private final DoubleArrayPublisher pose;
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

        pose = nt.getDoubleArrayTopic("sim/Pose").publish();
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
        pose.set(FieldPose.of(p.x(), p.y(), p.heading()));
        mode.set(String.valueOf(harness.robot.follower.mode()));

        wheels[0].set(harness.motors.get(Constants.frontLeftName).power);
        wheels[1].set(harness.motors.get(Constants.frontRightName).power);
        wheels[2].set(harness.motors.get(Constants.backLeftName).power);
        wheels[3].set(harness.motors.get(Constants.backRightName).power);

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

    @Override
    public void close() {
        nt.stopServer();
        nt.close();
    }
}
