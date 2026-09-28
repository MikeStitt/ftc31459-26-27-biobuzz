package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TeleOps and autos now share one parent. These check that both halves still
 * get what they used to: hardware at init, the scheduler, the shadow
 * localizers, Panels, and a follower that is stopped at the end.
 */
public class SharedLifecycleTest {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** A minimal teleop: records when each hook ran. */
    public static class SampleTeleOp extends CorbelsTeleOp {
        public final List<String> calls = new ArrayList<>();

        @Override protected void bindings() { calls.add("bindings"); }
        @Override protected void shadowLocalizers() { calls.add("shadowLocalizers"); }

        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() {
            loopBefore();
            calls.add("drive");
            Tracker.publish("teleop/loops", Tracker.loopCount());
            loopAfter();
        }
    }

    /** A minimal auto. */
    public static class SampleAutoOpMode extends CorbelsAuto {
        public final List<String> calls = new ArrayList<>();
        public boolean ran;

        @Override protected Pose startPose() {
            calls.add("startPose");
            return POSES.of(24, 24, 45);
        }

        @Override protected Command routine() {
            calls.add("routine");
            return Commands.instant(() -> ran = true);
        }

        @Override protected void shadowLocalizers() { calls.add("shadowLocalizers"); }

        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    @Test
    public void aTeleOpGetsHardwareAtInitAndItsHooksInOrder() {
        SampleTeleOp opMode = new SampleTeleOp();
        OpModeHarness h = new OpModeHarness(opMode);

        h.init();
        assertNotNull("hardware resolved at init", opMode.hardware);
        assertEquals("no hooks yet", 0, opMode.calls.size());

        h.start();
        assertEquals("bindings before the shadow localizers",
                "[bindings, shadowLocalizers]", opMode.calls.toString());
        assertNotNull(opMode.buttons);

        h.loop();
        assertTrue(opMode.calls.contains("drive"));
        assertEquals("one loop finished", 1, Tracker.loopCount());
        assertEquals("and none had finished while its body ran",
                0.0, (Double) Tracker.values().get("teleop/loops"), 1e-9);

        h.stop();
        assertEquals("stopped", 0.0, h.forward(), 1e-9);
    }

    @Test
    public void anAutoGetsTheSameTreatmentPlusItsStartPoseAndRoutine() {
        SampleAutoOpMode opMode = new SampleAutoOpMode();
        OpModeHarness h = new OpModeHarness(opMode);

        h.init();
        assertNotNull("hardware resolved at init, for autos too", opMode.hardware);
        assertTrue("the start pose is set during init", opMode.calls.contains("startPose"));
        assertEquals("placed where it said", 24.0, h.robot.follower.pose().x(), 1e-6);

        h.start();
        assertTrue(opMode.calls.contains("routine"));
        assertTrue(opMode.calls.contains("shadowLocalizers"));

        h.loop();
        assertTrue("the scheduler ran the routine", opMode.ran);
        assertEquals(1, Tracker.loopCount());

        h.stop();
        assertEquals(0.0, h.forward(), 1e-9);
    }

    @Test
    public void bothResolveTheHardwareExactlyOnce() {
        OpModeHarness teleop = new OpModeHarness(new SampleTeleOp());
        teleop.init();
        teleop.start();
        teleop.loops(5, 0);
        assertEquals("once, at init", 1, teleop.lookups);

        OpModeHarness auto = new OpModeHarness(new SampleAutoOpMode());
        auto.init();
        auto.start();
        auto.loops(5, 0);
        assertEquals("and the same for an auto", 1, auto.lookups);
    }
}
