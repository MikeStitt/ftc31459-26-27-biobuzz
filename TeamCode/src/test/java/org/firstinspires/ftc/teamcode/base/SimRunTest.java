package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.lessons.L2p2TankOpMode;
import org.junit.Test;

/**
 * How {@link SimRun} reads its arguments. The run itself is not a test: it
 * goes until Ctrl-C, and what it is for is a person watching AdvantageScope.
 */
public class SimRunTest {

    @Test
    public void aLessonIsNamedWithoutItsPackage() throws Exception {
        assertTrue("L2p2TankOpMode", SimRun.lesson("L2p2TankOpMode") instanceof L2p2TankOpMode);
    }

    @Test
    public void aStickIsSetByItsGamepadName() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2p2TankOpMode());
        SimRun.set(h, "left_stick_y=-1");
        SimRun.set(h, "right_stick_y=-0.5");
        assertEquals(-1.0f, h.gamepad1.left_stick_y, 0);
        assertEquals(-0.5f, h.gamepad1.right_stick_y, 0);
    }

    @Test
    public void aButtonIsSetTheSameWay() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2p2TankOpMode());
        SimRun.set(h, "a=true");
        assertTrue("a is pressed", h.gamepad1.a);
    }

    @Test
    public void aMisspeltFieldSaysWhatTheSticksAreCalled() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2p2TankOpMode());
        try {
            SimRun.set(h, "leftY=-1");
            fail("a field that does not exist should not be silently ignored");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("left_stick_y"));
        }
    }

    @Test
    public void anArgumentWithNoValueIsRejected() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2p2TankOpMode());
        try {
            SimRun.set(h, "left_stick_y");
            fail("name=value is the only shape there is");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("name=value"));
        }
    }
}
