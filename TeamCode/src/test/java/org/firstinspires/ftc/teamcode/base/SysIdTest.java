package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.sysid.SysIdDriveOpMode;
import org.firstinspires.ftc.teamcode.sysid.VoltageResponseOpMode;
import org.junit.Test;

/**
 * The data SysId reads. Its analyser splits the test-state string on the last
 * dash and throws away anything it does not recognise, so these values are not
 * ours to choose.
 */
public class SysIdTest {

    @Test
    public void theStateStringsAreTheOnesTheAnalyserAccepts() {
        assertEquals("quasistatic-forward", SysIdRecorder.State.QUASISTATIC_FORWARD.toString());
        assertEquals("quasistatic-reverse", SysIdRecorder.State.QUASISTATIC_REVERSE.toString());
        assertEquals("dynamic-forward", SysIdRecorder.State.DYNAMIC_FORWARD.toString());
        assertEquals("dynamic-reverse", SysIdRecorder.State.DYNAMIC_REVERSE.toString());
        assertEquals("none", SysIdRecorder.State.NONE.toString());
    }

    @Test
    public void everyStateSplitsIntoATestAndADirectionTheAnalyserKnows() {
        for (SysIdRecorder.State state : SysIdRecorder.State.values()) {
            if (state == SysIdRecorder.State.NONE) continue;
            String text = state.toString();
            int dash = text.lastIndexOf('-');
            String test = text.substring(0, dash);
            String direction = text.substring(dash + 1);
            assertTrue(test, test.equals("quasistatic") || test.equals("dynamic"));
            assertTrue(direction, direction.equals("forward") || direction.equals("reverse"));
        }
    }

    @Test
    public void appliedVoltsAreThePowerTimesWhateverTheBatteryIsGiving() {
        assertEquals(12.4, SysIdRecorder.appliedVolts(1.0, 12.4), 1e-9);
        assertEquals("a flatter battery gives less for the same power",
                5.5, SysIdRecorder.appliedVolts(0.5, 11.0), 1e-9);
        assertEquals(-6.2, SysIdRecorder.appliedVolts(-0.5, 12.4), 1e-9);
    }

    @Test
    public void theQuasistaticTestRampsAndTheDynamicOneSteps() {
        assertEquals("ramps from nothing", 0.0,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.QUASISTATIC_FORWARD, 0, 0.25, 4), 1e-9);
        assertEquals("a quarter volt per second", 2.5,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.QUASISTATIC_FORWARD, 10, 0.25, 4), 1e-9);
        assertEquals("backwards is the same ramp, negative", -2.5,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.QUASISTATIC_REVERSE, 10, 0.25, 4), 1e-9);
        assertEquals("the step is there from the first instant", 4.0,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.DYNAMIC_FORWARD, 0, 0.25, 4), 1e-9);
        assertEquals(-4.0,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.DYNAMIC_REVERSE, 5, 0.25, 4), 1e-9);
        assertEquals("nothing when no test is running", 0.0,
                SysIdDriveOpMode.voltsFor(SysIdRecorder.State.NONE, 5, 0.25, 4), 1e-9);
    }

    @Test
    public void theVoltageResponseTestWalksItsPhasesInOrder() {
        assertEquals("idle first", 0, VoltageResponseOpMode.phaseAt(0));
        assertEquals(0, VoltageResponseOpMode.phaseAt(2.9));
        assertEquals("then 1 Hz", 1, VoltageResponseOpMode.phaseAt(3.1));
        assertEquals("then 20 Hz", 5, VoltageResponseOpMode.phaseAt(16));
        assertEquals("and past the end", 6, VoltageResponseOpMode.phaseAt(18.5));
    }

    @Test
    public void theSquareWaveAlternatesAtTheFrequencyForThatPhase() {
        // Phase 0 is idle, whatever the time.
        assertEquals(0.0, VoltageResponseOpMode.powerAt(0, 1.0, 0.4), 1e-9);

        // Phase 1 is 1 Hz: high for the first half second of each second.
        assertEquals(0.4, VoltageResponseOpMode.powerAt(1, 3.1, 0.4), 1e-9);
        assertEquals(-0.4, VoltageResponseOpMode.powerAt(1, 3.6, 0.4), 1e-9);
        assertEquals(0.4, VoltageResponseOpMode.powerAt(1, 4.1, 0.4), 1e-9);

        // Phase 5 is 20 Hz: a full cycle every 50 ms.
        assertEquals(0.4, VoltageResponseOpMode.powerAt(5, 15.0, 0.4), 1e-9);
        assertEquals(-0.4, VoltageResponseOpMode.powerAt(5, 15.030, 0.4), 1e-9);

        assertEquals("nothing after the last phase", 0.0,
                VoltageResponseOpMode.powerAt(6, 20.0, 0.4), 1e-9);
    }

    @Test
    public void theVoltageResponseOpModeIsIdleUntilTheTriggerIsHeld() {
        OpModeHarness h = new OpModeHarness(new VoltageResponseOpMode());
        h.init();
        h.start();
        h.loop();
        assertEquals(false, Tracker.values().get("running"));
        assertEquals(0.0, (Double) Tracker.values().get("drive/power"), 1e-9);

        h.gamepad1.right_trigger = 1.0f;
        h.loops(2, 5);
        assertEquals(true, Tracker.values().get("running"));
        assertTrue("it times its own reads",
                (Double) Tracker.values().get("volts/call_us") >= 0);
        assertEquals("a nominal battery in tests", 12.0,
                (Double) Tracker.values().get("volts"), 1e-9);
    }

    @Test
    public void theChannelNamesFollowWpilibsConvention() {
        SysIdRecorder recorder = new SysIdRecorder(null, "drive");
        assertEquals("sysid-test-state-drive", recorder.stateChannel());
    }

    @Test
    public void theOpModeRunsOnlyWhileTheTriggerIsHeld() {
        OpModeHarness h = new OpModeHarness(new SysIdDriveOpMode());
        h.init();
        h.start();
        h.loop();
        assertEquals(false, Tracker.values().get("sysid/running"));
        assertEquals("nothing moves until asked", 0.0,
                (Double) Tracker.values().get("sysid/power"), 1e-9);

        h.gamepad1.right_trigger = 1.0f;
        h.loops(3, 10);
        assertEquals(true, Tracker.values().get("sysid/running"));
        assertTrue("and then it ramps", (Double) Tracker.values().get("sysid/volts_commanded") > 0);

        h.gamepad1.right_trigger = 0f;
        h.loop();
        assertEquals(false, Tracker.values().get("sysid/running"));
        assertEquals(0.0, (Double) Tracker.values().get("sysid/power"), 1e-9);
    }

    @Test
    public void aFlatterBatteryMeansMorePowerForTheSameVolts() {
        OpModeHarness h = new OpModeHarness(new SysIdDriveOpMode());
        h.batteryVolts = 12.0;
        h.init();
        h.start();
        h.gamepad1.dpad_right = true;              // dynamic forward: a 4 V step
        h.loop();
        h.gamepad1.right_trigger = 1.0f;
        h.loop();
        assertEquals(4.0 / 12.0, (Double) Tracker.values().get("sysid/power"), 1e-6);

        h.batteryVolts = 10.0;
        h.loop();
        assertEquals("same volts asked for, more power needed",
                4.0 / 10.0, (Double) Tracker.values().get("sysid/power"), 1e-6);
    }
}
