package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.lessons.L2bTankOpMode;
import org.junit.Test;

/**
 * How {@link SimRun} reads its arguments. The run itself is not a test: it
 * goes until Ctrl-C, and what it is for is a person watching AdvantageScope.
 */
public class SimRunTest {

    @Test
    public void aLessonIsNamedWithoutItsPackage() throws Exception {
        assertTrue("L2bTankOpMode", SimRun.lesson("L2bTankOpMode") instanceof L2bTankOpMode);
    }

    @Test
    public void aStickIsSetByItsGamepadName() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2bTankOpMode());
        SimRun.set(h, "left_stick_y=-1");
        SimRun.set(h, "right_stick_y=-0.5");
        assertEquals(-1.0f, h.gamepad1.left_stick_y, 0);
        assertEquals(-0.5f, h.gamepad1.right_stick_y, 0);
    }

    @Test
    public void aButtonIsSetTheSameWay() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2bTankOpMode());
        SimRun.set(h, "a=true");
        assertTrue("a is pressed", h.gamepad1.a);
    }

    @Test
    public void aMisspeltFieldSaysWhatTheSticksAreCalled() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2bTankOpMode());
        try {
            SimRun.set(h, "leftY=-1");
            fail("a field that does not exist should not be silently ignored");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("left_stick_y"));
        }
    }

    @Test
    public void padWithAStickIsInvalid() {
        String why = SimRun.invalid(new String[] {"L2bTankOpMode", "pad", "left_stick_y=-1"});
        assertTrue(String.valueOf(why), why != null && why.contains("left_stick_y=-1"));
        assertTrue(String.valueOf(why), why.startsWith("invalid arguments:"));
    }

    @Test
    public void padWithSeveralSettingsNamesThemAll() {
        String why = SimRun.invalid(
                new String[] {"L2bTankOpMode", "left_stick_y=-1", "pad", "a=true"});
        assertTrue(String.valueOf(why), why.contains("left_stick_y=-1"));
        assertTrue(String.valueOf(why), why.contains("a=true"));
    }

    @Test
    public void padOnItsOwnIsFine() {
        assertEquals(null, SimRun.invalid(new String[] {"L2bTankOpMode", "pad"}));
        assertEquals(null, SimRun.invalid(new String[] {"L2bTankOpMode", "pad", "pad"}));
    }

    @Test
    public void settingsOnTheirOwnAreFine() {
        assertEquals(null,
                SimRun.invalid(new String[] {"L2bTankOpMode", "left_stick_y=-1", "a=true"}));
        assertEquals(null, SimRun.invalid(new String[] {"L2bTankOpMode"}));
        assertEquals(null, SimRun.invalid(new String[] {}));
    }

    @Test
    public void onlyTheBareWordIsTheFlag() {
        // pad=true is a setting, so it reaches set() and fails on its own name.
        // It needs a real setting beside it to tell a bare-word match from a
        // prefix match: on its own there is nothing for the flag to conflict with.
        assertEquals(null, SimRun.invalid(new String[] {"L2bTankOpMode", "pad=true"}));
        assertEquals(null,
                SimRun.invalid(new String[] {"L2bTankOpMode", "pad=true", "a=true"}));
    }

    @Test
    public void theLessonNameIsNeverASetting() {
        // args[0] is the lesson, so a lesson called pad would not be the flag.
        assertEquals(null, SimRun.invalid(new String[] {"pad", "left_stick_y=-1"}));
    }

    @Test
    public void anArgumentWithNoValueIsRejected() throws Exception {
        OpModeHarness h = new OpModeHarness(new L2bTankOpMode());
        try {
            SimRun.set(h, "left_stick_y");
            fail("name=value is the only shape there is");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("name=value"));
        }
    }
}
