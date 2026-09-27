package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

import org.junit.Test;

import java.io.File;

/**
 * Every run writes a WPILOG file beside the live Panels view. These check that
 * it happens, that it carries what a lesson logged, and that it is closed.
 */
public class FlightLogTest {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** A lesson that logs one of everything. */
    public static class SampleTeleOp extends CorbelsTeleOp {
        @Override public void init() { initBefore(); initAfter(); }

        @Override
        public void loop() {
            loopBefore();
            Tracker.publish("number", 1.5);
            Tracker.publish("flag", true);
            Tracker.publish("text", "hello");
            Tracker.publish("Robot/Pose", POSES.of(24, 48, 90));
            loopAfter();
        }
    }

    public static class SampleAutoOpMode extends CorbelsAuto {
        @Override public void init() { initBefore(); initAfter(); }
        @Override public void loop() { loopBefore(); loopAfter(); }

        @Override protected Pose startPose() { return POSES.of(12, 12, 0); }
        @Override protected com.pedropathing.ivy.Command routine() {
            return com.pedropathing.ivy.commands.Commands.instant(() -> { });
        }
    }

    @Test
    public void aTeleOpRunWritesOneFlightLog() {
        OpModeHarness h = new OpModeHarness(new SampleTeleOp());
        assertEquals("nothing before init", 0, h.logs().length);
        h.init();
        assertEquals("a file from init, so setting-up values are kept", 1, h.logs().length);

        h.start();
        assertEquals("and still just the one", 1, h.logs().length);
        assertTrue(Tracker.flightlog.status(), Tracker.flightlog.isRecording());

        h.loops(5, 0);
        h.stop();
        assertFalse("closed when the OpMode ends", Tracker.flightlog.isRecording());
        assertTrue("and has something in it", h.logs()[0].length() > 0);
    }

    @Test
    public void anAutoWritesOneToo() {
        OpModeHarness h = new OpModeHarness(new SampleAutoOpMode());
        h.init();
        h.start();
        h.loops(3, 0);
        h.stop();
        assertEquals(1, h.logs().length);
        assertFalse(Tracker.flightlog.isRecording());
    }

    @Test
    public void whatALessonLogsGoesToPanelsAndTheFileBoth() {
        OpModeHarness h = new OpModeHarness(new SampleTeleOp());
        h.init();
        h.start();
        h.loop();

        // Panels, as before.
        assertEquals(1.5, (Double) Tracker.values().get("number"), 1e-9);
        assertEquals(true, Tracker.values().get("flag"));
        assertEquals("hello", Tracker.values().get("text"));
        assertEquals(24.0, (Double) Tracker.values().get("Robot/Pose/x_in"), 1e-9);

        h.stop();

        // And the file, which by now is closed and readable.
        File file = h.logs()[0];
        assertTrue("the file has content", file.length() > 200);
    }

    @Test
    public void somethingLoggedDuringInitIsKept() {
        OpModeHarness h = new OpModeHarness(new InitLogging());
        h.init();
        assertEquals("init values reach the file before Panels exists",
                7.5, (Double) Tracker.values().get("setup/value"), 1e-9);
        h.start();
        h.stop();
        assertTrue(h.logs()[0].length() > 0);
    }

    /** Logs during init, when Panels does not exist yet. */
    public static class InitLogging extends CorbelsTeleOp {
        @Override protected void onInit() { Tracker.publish("setup/value", 7.5); }
        @Override public void init() { initBefore(); initAfter(); }
        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    @Test
    public void theFileNameSaysWhichOpModeItWas() {
        OpModeHarness h = new OpModeHarness(new SampleTeleOp());
        h.init();
        h.start();
        h.stop();
        assertTrue(h.logs()[0].getName(), h.logs()[0].getName().startsWith("SampleTeleOp-"));
    }
}
