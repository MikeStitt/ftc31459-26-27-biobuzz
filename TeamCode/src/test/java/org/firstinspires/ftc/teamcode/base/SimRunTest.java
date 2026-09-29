package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.example.DriveForward24OpMode;
import org.firstinspires.ftc.teamcode.lessons.L2bTankOpMode;
import org.junit.Test;

/**
 * How {@link SimRun} finds an OpMode and fills a gamepad. Which arguments are
 * legal is {@link SimArgs}'s, and SimArgsTest has it. The run itself is not a
 * test: it goes until Ctrl-C, and what it is for is a person watching
 * AdvantageScope.
 */
public class SimRunTest {

    @Test
    public void anOpModeIsNamedByWhatFollowsThePackage() throws Exception {
        assertTrue("a lesson",
                SimRun.opMode("lessons.L2bTankOpMode") instanceof L2bTankOpMode);
        assertTrue("another package",
                SimRun.opMode("example.DriveForward24OpMode") instanceof DriveForward24OpMode);
    }

    /**
     * No OpMode sits directly in {@code teamcode} today, so that case is shown
     * by the prefix being one constant that the name is appended to.
     */
    @Test
    public void thePackageIsWrittenInOnePlace() {
        assertEquals("org.firstinspires.ftc.teamcode.", SimRun.PACKAGE);
    }

    @Test
    public void aMisspeltOpModeIsNotFound() {
        try {
            SimRun.opMode("lessons.NoSuchOpMode");
            fail("a class that does not exist should not be silently ignored");
        } catch (ReflectiveOperationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("NoSuchOpMode"));
        }
    }

    @Test
    public void aStickIsSetByItsGamepadName() throws Exception {
        Gamepad typed = new Gamepad();
        SimRun.set(typed, "left_stick_y=-1");
        SimRun.set(typed, "right_stick_y=-0.5");
        assertEquals(-1.0f, typed.left_stick_y, 0);
        assertEquals(-0.5f, typed.right_stick_y, 0);
    }

    @Test
    public void aButtonIsSetTheSameWay() throws Exception {
        Gamepad typed = new Gamepad();
        SimRun.set(typed, "a=true");
        assertTrue("a is pressed", typed.a);
    }

    /**
     * A control option is a control held from before the run starts, so its
     * press edge belongs to the first loop and to no later one. That falls out
     * of copying the same staging pad every loop, which is what the run does.
     */
    @Test
    public void aTypedControlFiresItsEdgeOnTheFirstLoopAndNoOther() throws Exception {
        Gamepad typed = new Gamepad();
        SimRun.set(typed, "a=true");
        Gamepad readByTheLesson = new Gamepad();

        readByTheLesson.copy(typed);
        assertTrue("a on the first loop", readByTheLesson.a);
        assertTrue("aWasPressed() on the first loop", readByTheLesson.aWasPressed());

        readByTheLesson.copy(typed);
        assertTrue("a on the second loop", readByTheLesson.a);
        assertEquals("aWasPressed() on the second loop",
                false, readByTheLesson.aWasPressed());
    }
}
