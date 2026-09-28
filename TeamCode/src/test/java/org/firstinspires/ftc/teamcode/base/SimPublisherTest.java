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

/**
 * The simulation's NetworkTables server: that it starts, and that what it
 * publishes is what the simulated robot is doing.
 *
 * <p>Ports 1799 and 5899 rather than the real 1735 and 5810, so a test never
 * fights a server someone left running.
 */
public class SimPublisherTest {

    /** Where a failure left its flight log. */
    @Rule
    public final SimLogs logs = new SimLogs();

    private static final int NT3 = 1799;
    private static final int NT4 = 5899;

    private static boolean accepts(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void theServerStartsAndListens() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.init();
        try (SimPublisher out = new SimPublisher(h, NT3, NT4)) {
            out.publish();
            assertTrue("a client can connect on the NT4 port", accepts(NT4));
            assertTrue("the instance is a server",
                    out.instance().getNetworkMode()
                            .contains(NetworkTableInstance.NetworkMode.kServer));
        }
        h.stop();
    }

    /** The three little-endian doubles of a {@code struct:Pose2d}. */
    private static double[] unpack(byte[] raw) {
        ByteBuffer b = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        return new double[]{b.getDouble(), b.getDouble(), b.getDouble()};
    }

    @Test
    public void theFieldViewIsToldWhatAPoseLooksLike() {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.init();
        try (SimPublisher out = new SimPublisher(h, NT3, NT4)) {
            out.publish();
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
        }
        h.stop();
    }

    @Test
    public void theRobotIsPublishedWhereTheFieldViewWantsIt() {
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
                byte[] raw = out.instance().getRawTopic("sim/Pose")
                        .subscribe("struct:Pose2d", new byte[0]).get();
                assertEquals("a Pose2d is three doubles", 24, raw.length);
                double[] pose = unpack(raw);
                double inches = h.robot.localizer.state().pose().x();
                assertEquals("x is metres from the centre of the field",
                        (inches - 72) * 0.0254, pose[0], 1e-9);
                assertEquals("y is still on the centre line", -72 * 0.0254, pose[1], 1e-9);

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
