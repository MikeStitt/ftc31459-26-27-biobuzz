package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.wpi.first.networktables.NetworkTableInstance;

import io.github.mikestitt.corbelsflightlog.FlightLog;

import org.junit.Rule;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * The simulation's NetworkTables server: that it starts, and that what it
 * publishes is what the simulated robot is doing.
 *
 * <p>One test rather than one per fact, because a second NetworkTables instance
 * in the same JVM aborts that JVM on macOS. Measured on macos-latest, Temurin
 * 17.0.20 on arm64: the first server logs
 * {@code NT: Listening on NT3 port 1800, NT4 port 5900} and 15 ms later the next
 * instance's startup gives {@code libc++abi: terminating due to uncaught
 * exception of type std::__1::system_error: mutex lock failed: Invalid argument}
 * and exit 134, with no test reported as failing. There is no
 * {@code hs_err_pid} file to read, because the abort is on a thread HotSpot does
 * not cover, and macOS writes no crash report for it on that runner either. It
 * failed three of the six runs made on 2026-09-30 and never once on this
 * laptop, which has sixteen cores against the runner's few: 15 runs of this
 * suite in one JVM, 600 sequential create-and-close cycles, and 600 more with
 * each close on a thread of its own so a teardown overlaps the next startup.
 *
 * <p>Standing one server up and asking it everything is also the simpler shape.
 * Binding a port is the one thing here that is not side-effect-free, and three
 * tests bound three of them to check facts that need no client at all.
 */
public class SimPublisherTest {

    /** Where a failure left its flight log. */
    @Rule
    public final SimLogs logs = new SimLogs();

    /**
     * 1799 and 5899 rather than the real 1735 and 5810, so the test never fights
     * a server someone left running.
     */
    private static final int NT3 = 1799;
    private static final int NT4 = 5899;

    /**
     * Whether a client can connect on this port, waiting for a listener to
     * appear rather than asking once.
     *
     * <p>{@code startServer} hands the work to a thread and returns, so the
     * socket is not bound when the constructor returns. A connect timeout is no
     * help: a port nothing is listening on is refused at once, so one attempt
     * either wins the race or reports a server that is on its way as absent.
     * That is what failed on windows-latest and nowhere else.
     */
    private static boolean accepts(int port) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (true) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
                return true;
            } catch (Exception e) {
                if (System.nanoTime() >= deadline) {
                    return false;
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /** The three little-endian doubles of a {@code struct:Pose2d}. */
    private static double[] unpack(byte[] raw) {
        ByteBuffer b = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        return new double[]{b.getDouble(), b.getDouble(), b.getDouble()};
    }

    @Test
    public void theServerPublishesWhatTheSimulatedRobotIsDoing() {
        int savedTurns = FlightLog.fieldQuarterTurns;
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        try {
            FlightLog.fieldQuarterTurns = 0;
            h.init();
            h.start();
            h.gamepad1.left_stick_y = -1.0f;
            h.gamepad1.right_stick_y = -1.0f;
            h.loops(200, 0);
            try (SimPublisher out = new SimPublisher(h, NT3, NT4)) {
                out.publish();

                assertTrue("a client can connect on the NT4 port", accepts(NT4));
                assertTrue("the instance is a server",
                        out.instance().getNetworkMode()
                                .contains(NetworkTableInstance.NetworkMode.kServer));

                assertEquals("the pose topic says it is a struct", "struct:Pose2d",
                        out.instance().getTopic("sim/Pose").getTypeString());
                String[][] expected = {
                        {"struct:Translation2d", "double x;double y"},
                        {"struct:Rotation2d", "double value"},
                        {"struct:Pose2d", "Translation2d translation;Rotation2d rotation"}};
                for (String[] schema : expected) {
                    String key = "/.schema/" + schema[0];
                    assertEquals(key + " is published as a schema", "structschema",
                            out.instance().getTopic(key).getTypeString());
                    byte[] raw = out.instance().getRawTopic(key)
                            .subscribe("structschema", new byte[0]).get();
                    assertEquals("what " + schema[0] + " is made of", schema[1],
                            new String(raw, StandardCharsets.UTF_8));
                }

                byte[] raw = out.instance().getRawTopic("sim/Pose")
                        .subscribe("struct:Pose2d", new byte[0]).get();
                assertEquals("a Pose2d is three doubles", 24, raw.length);
                double[] pose = unpack(raw);
                double inches = h.robot.localizer.state().pose().x();
                assertEquals("x is metres from the centre of the field",
                        (inches - 72) * 0.0254, pose[0], 1e-9);
                double acrossIn = h.robot.localizer.state().pose().y();
                assertEquals("y is metres from the centre of the field",
                        (acrossIn - 72) * 0.0254, pose[1], 1e-9);

                double[] wheels = {
                        out.instance().getDoubleTopic("sim/wheels/frontLeft").subscribe(0).get(),
                        out.instance().getDoubleTopic("sim/wheels/frontRight").subscribe(0).get(),
                        out.instance().getDoubleTopic("sim/wheels/backLeft").subscribe(0).get(),
                        out.instance().getDoubleTopic("sim/wheels/backRight").subscribe(0).get()};
                assertArrayEquals("all four wheels at full power",
                        new double[]{1, 1, 1, 1}, wheels, 1e-9);

                assertEquals("the stick the driver is holding", -1.0,
                        out.instance().getDoubleTopic("sim/stick/leftY").subscribe(0).get(), 1e-9);
                assertTrue("moving forward at a fair speed",
                        out.instance().getDoubleTopic("sim/vel/forward_ips")
                                .subscribe(0).get() > 50);
            }
            h.stop();
        } finally {
            FlightLog.fieldQuarterTurns = savedTurns;
        }
    }
}
