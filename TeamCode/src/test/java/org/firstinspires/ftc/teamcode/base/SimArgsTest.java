package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Every form {@link SimArgs} can read and every way an argument list can be
 * wrong. Each set is tested for every member by looping over the set the code
 * holds, so a control added later is covered without this file being edited.
 */
public final class SimArgsTest {

    private static SimArgs.Plan parse(String... args) {
        return SimArgs.parse(args);
    }

    /** A value of the right kind for one control. */
    private static String value(String control) {
        return SimArgs.AXES.contains(control) ? "0.5" : "true";
    }

    // --- the four forms, and --help ----------------------------------------

    @Test
    public void padCheckOnItsOwnIsTheGamepadForm() {
        SimArgs.Plan plan = parse("--pad-check");
        assertNull(plan.error);
        assertEquals(SimArgs.Form.PAD_CHECK, plan.form);
        assertNull("no OpMode", plan.opMode);
    }

    @Test
    public void anOpModeOnItsOwnLeavesEveryControlUntouched() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode");
        assertNull(plan.error);
        assertEquals(SimArgs.Form.OPMODE, plan.form);
        assertEquals("lessons.L2bTankOpMode", plan.opMode);
        assertEquals(0, plan.controls.size());
    }

    @Test
    public void anOpModeWithPadReadsGamepads() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--pad");
        assertNull(plan.error);
        assertEquals(SimArgs.Form.OPMODE_PAD, plan.form);
    }

    @Test
    public void anOpModeWithControlsCarriesThemInOrder() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--left_stick_y=-1", "--a=true");
        assertNull(plan.error);
        assertEquals(SimArgs.Form.OPMODE_CONTROLS, plan.form);
        assertEquals("[left_stick_y=-1, a=true]", plan.controls.toString());
    }

    @Test
    public void helpIsItsOwnForm() {
        SimArgs.Plan plan = parse("--help");
        assertNull(plan.error);
        assertEquals(SimArgs.Form.HELP, plan.form);
    }

    // --- the sets, every member of each ------------------------------------

    @Test
    public void everyControlIsAnOptionAndNamesAFieldOfAGamepad() throws Exception {
        assertEquals("the 21 controls a gamepad has", 21, SimArgs.CONTROLS.size());
        List<String> refused = new ArrayList<>();
        Gamepad staging = new Gamepad();
        for (String control : SimArgs.CONTROLS) {
            String option = "--" + control + "=" + value(control);
            SimArgs.Plan plan = parse("lessons.L2bTankOpMode", option);
            if (plan.error != null) {
                refused.add(option + ": " + plan.error);
                continue;
            }
            SimRun.set(staging, plan.controls.get(0));
        }
        assertEquals("controls the parser would not take", "[]", refused.toString());
        assertEquals("left_stick_y reached its field", 0.5f, staging.left_stick_y, 0f);
        assertTrue("a reached its field", staging.a);
    }

    @Test
    public void everyDerivedFieldIsRefusedByName() {
        assertEquals("the nine the SDK fills for itself", 9, SimArgs.DERIVED.size());
        for (String derived : SimArgs.DERIVED) {
            SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--" + derived + "=true");
            assertTrue(derived + " was accepted", plan.error != null);
            assertTrue(plan.error, plan.error.contains("\"" + derived + "\""));
        }
    }

    @Test
    public void everyTouchpadFieldIsRefusedByName() {
        assertEquals("the seven touchpad fields", 7, SimArgs.TOUCHPAD.size());
        for (String touchpad : SimArgs.TOUCHPAD) {
            SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--" + touchpad + "=true");
            assertTrue(touchpad + " was accepted", plan.error != null);
            assertTrue(plan.error, plan.error.contains("\"" + touchpad + "\""));
        }
    }

    /**
     * A field the SDK adds later is refused until somebody decides which group
     * it belongs in, rather than being settable by accident.
     */
    @Test
    public void everyOtherFieldOfAGamepadIsRefused() {
        List<String> accepted = new ArrayList<>();
        for (Field field : Gamepad.class.getFields()) {
            String name = field.getName();
            if (Modifier.isStatic(field.getModifiers())
                    || SimArgs.CONTROLS.contains(name)
                    || SimArgs.DERIVED.contains(name)
                    || SimArgs.TOUCHPAD.contains(name)) {
                continue;
            }
            if (parse("lessons.L2bTankOpMode", "--" + name + "=1").error == null) {
                accepted.add(name);
            }
        }
        assertEquals("fields in no group that were accepted anyway", "[]", accepted.toString());
    }

    @Test
    public void aConstantIsNotAControl() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--DEFAULT_TRIGGER_THRESHOLD=0.9");
        assertTrue("a static field was accepted", plan.error != null);
        assertTrue(plan.error, plan.error.contains("no control called"));
    }

    // --- the five usage errors ---------------------------------------------

    @Test
    public void noArgumentsIsAUsageError() {
        assertTrue(parse().error != null);
    }

    @Test
    public void anUnknownControlSaysThereIsNoSuchControl() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--leftY=-1");
        assertTrue(plan.error, plan.error.contains("no control called \"leftY\""));
    }

    @Test
    public void aValueOfTheWrongTypeSaysWhatTheControlTakes() {
        SimArgs.Plan axis = parse("lessons.L2bTankOpMode", "--left_stick_y=sideways");
        assertTrue(axis.error, axis.error.contains("number"));
        assertTrue(axis.error, axis.error.contains("\"sideways\""));
        SimArgs.Plan button = parse("lessons.L2bTankOpMode", "--a=0.5");
        assertTrue(button.error, button.error.contains("true or false"));
    }

    @Test
    public void padWithAControlOptionNamesTheOptionThatClashed() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--pad", "--left_stick_y=-1");
        assertTrue(plan.error, plan.error.contains("--left_stick_y=-1"));
    }

    @Test
    public void aControlWithNoValueIsRejected() {
        SimArgs.Plan plan = parse("lessons.L2bTankOpMode", "--left_stick_y");
        assertTrue(plan.error, plan.error.contains("--name=value"));
    }

    @Test
    public void theOldSpellingsAreNotTheNewOnes() {
        assertTrue("the bare word pad", parse("lessons.L2bTankOpMode", "pad").error != null);
        assertTrue("a control with no dashes",
                parse("lessons.L2bTankOpMode", "left_stick_y=-1").error != null);
        assertTrue("a flag where the OpMode goes", parse("--pad").error != null);
        assertTrue("pad-check with an OpMode", parse("--pad-check", "lessons.L2bTankOpMode")
                .error != null);
    }

    @Test
    public void everyErrorLineNamesTheToolAndTheHelp() {
        String[][] bad = {
            {},
            {"lessons.L2bTankOpMode", "--leftY=-1"},
            {"lessons.L2bTankOpMode", "--a=0.5"},
            {"lessons.L2bTankOpMode", "--pad", "--a=true"},
            {"lessons.L2bTankOpMode", "pad"},
            {"--pad-check", "lessons.L2bTankOpMode"},
            {"--nonsense"},
        };
        for (String[] args : bad) {
            String error = SimArgs.parse(args).error;
            assertTrue("no error for " + String.join(" ", args), error != null);
            assertTrue(error, error.startsWith("simRun: "));
            assertTrue(error, error.endsWith("; see --help"));
            assertEquals("one line", 1, error.split("\n").length);
        }
    }

    // --- the usage text ----------------------------------------------------

    @Test
    public void theUsageNamesEveryFieldOfAllThreeGroups() {
        String usage = SimArgs.usage();
        List<String> missing = new ArrayList<>();
        for (String name : SimArgs.CONTROLS) {
            if (!usage.contains(name)) {
                missing.add(name);
            }
        }
        for (String name : SimArgs.DERIVED) {
            if (!usage.contains(name)) {
                missing.add(name);
            }
        }
        for (String name : SimArgs.TOUCHPAD) {
            if (!usage.contains(name)) {
                missing.add(name);
            }
        }
        assertEquals("fields the usage does not name", "[]", missing.toString());
    }

    @Test
    public void theUsageNamesAllFourFormsAndThePackage() {
        String usage = SimArgs.usage();
        assertTrue("--pad-check", usage.contains("--pad-check"));
        assertTrue("--pad", usage.contains("--pad "));
        assertTrue("--help", usage.contains("--help"));
        assertTrue("a control option", usage.contains("--control=value"));
        assertTrue("the package, without the dot that joins it to a name",
                usage.contains(SimArgs.packageName()));
        assertFalse("the dot that joins the package to a name",
                usage.contains(SimRun.PACKAGE + ":"));
    }
}
