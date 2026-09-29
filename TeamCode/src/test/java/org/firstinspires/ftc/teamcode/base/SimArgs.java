package org.firstinspires.ftc.teamcode.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link SimRun}'s arguments: which form an argument list is, which controls it
 * sets, or one line saying what is wrong with it.
 *
 * <p>Pure, so all of it is tested with no gamepad, no process and no console.
 * {@code SimRun} does the printing, the exit code and the running.
 *
 * <p>The four forms and the field groups are the ones in
 * {@code .docs/simRunRequirementsREADME.md}, and the usage text is printed from
 * the three sets below rather than from a list kept beside them.
 *
 * <p>Passes when: SimArgsTest.
 */
final class SimArgs {

    private SimArgs() {
    }

    /** The six stick and trigger axes, named as the FTC SDK names them. */
    static final Set<String> AXES = set(
            "left_stick_x", "left_stick_y", "right_stick_x", "right_stick_y",
            "left_trigger", "right_trigger");

    /** The fifteen buttons. */
    static final Set<String> BUTTONS = set(
            "a", "b", "x", "y", "start", "back", "guide",
            "left_bumper", "right_bumper", "left_stick_button", "right_stick_button",
            "dpad_up", "dpad_down", "dpad_left", "dpad_right");

    /** The 21 controls a gamepad has, which are the whole option set. */
    static final Set<String> CONTROLS = both(AXES, BUTTONS);

    /** The nine fields the SDK fills from those 21. Correct to read, not settable. */
    static final Set<String> DERIVED = set(
            "cross", "circle", "square", "triangle", "share", "options", "ps",
            "left_trigger_pressed", "right_trigger_pressed");

    /** The seven touchpad fields, which are not supported. */
    static final Set<String> TOUCHPAD = set(
            "touchpad", "touchpad_finger_1", "touchpad_finger_2",
            "touchpad_finger_1_x", "touchpad_finger_1_y",
            "touchpad_finger_2_x", "touchpad_finger_2_y");

    /** What an argument list asks for. */
    enum Form {
        /** {@code --help}. */
        HELP,
        /** {@code --pad-check}: gamepads and nothing else. */
        PAD_CHECK,
        /** An OpMode with every control untouched. */
        OPMODE,
        /** An OpMode reading real gamepads. */
        OPMODE_PAD,
        /** An OpMode with controls held from before it starts. */
        OPMODE_CONTROLS
    }

    /** One argument list read: a form and what it named, or the error line. */
    static final class Plan {

        final Form form;

        /** The OpMode's name after the package, or null for the first two forms. */
        final String opMode;

        /** The control options as {@code name=value}, without their {@code --}. */
        final List<String> controls;

        /** The whole line to print on standard error, or null when there is none. */
        final String error;

        private Plan(Form form, String opMode, List<String> controls, String error) {
            this.form = form;
            this.opMode = opMode;
            this.controls = Collections.unmodifiableList(controls);
            this.error = error;
        }
    }

    /**
     * What the arguments ask for, or what is wrong with them.
     *
     * <p>A usage error comes back as one line in {@link Plan#error}, quoting the
     * argument at fault and ending by naming {@code --help}. Every form and
     * every error is decided here, so the decision can be tested without
     * running anything.
     */
    static Plan parse(String[] args) {
        if (args.length == 0) {
            return bad("an OpMode class or --pad-check is needed");
        }
        String first = args[0];
        if (first.equals("--help")) {
            return new Plan(Form.HELP, null, empty(), null);
        }
        if (first.equals("--pad-check")) {
            if (args.length > 1) {
                return bad("\"--pad-check\" runs no OpMode, so it takes nothing else; got "
                        + quote(args[1]));
            }
            return new Plan(Form.PAD_CHECK, null, empty(), null);
        }
        if (first.startsWith("-")) {
            return bad("an OpMode class comes first; got " + quote(first));
        }
        boolean pad = false;
        List<String> controls = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--pad")) {
                pad = true;
                continue;
            }
            if (!arg.startsWith("--")) {
                return bad("expected --pad or a control option such as --left_stick_y=-1; got "
                        + quote(arg));
            }
            String setting = arg.substring(2);
            String wrong = wrongControl(setting);
            if (wrong != null) {
                return bad(wrong);
            }
            controls.add(setting);
        }
        if (pad && !controls.isEmpty()) {
            return bad("--pad reads the controls from a gamepad, so it cannot be combined with "
                    + quote("--" + controls.get(0)));
        }
        if (pad) {
            return new Plan(Form.OPMODE_PAD, first, empty(), null);
        }
        if (controls.isEmpty()) {
            return new Plan(Form.OPMODE, first, empty(), null);
        }
        return new Plan(Form.OPMODE_CONTROLS, first, controls, null);
    }

    /**
     * Why one {@code name=value} is not a control option, or null if it is.
     *
     * <p>The 21 controls are the whole option set. A field the SDK derives would
     * be overwritten on the next loop, and a touchpad field is not read at all,
     * so both say what they are rather than failing on a type.
     */
    private static String wrongControl(String setting) {
        int equals = setting.indexOf('=');
        if (equals < 1) {
            return "a control option is --name=value; got " + quote("--" + setting);
        }
        String name = setting.substring(0, equals);
        String value = setting.substring(equals + 1);
        if (DERIVED.contains(name)) {
            return quote(name) + " is filled by the SDK from the 21 controls, so setting it would"
                    + " be undone on the next loop";
        }
        if (TOUCHPAD.contains(name)) {
            return quote(name) + " is a touchpad field, and the touchpad is not read";
        }
        if (AXES.contains(name)) {
            if (!isNumber(value)) {
                return quote(name) + " is a stick or trigger, so its value is a number; got "
                        + quote(value);
            }
            return null;
        }
        if (BUTTONS.contains(name)) {
            if (!value.equals("true") && !value.equals("false")) {
                return quote(name) + " is a button, so its value is true or false; got "
                        + quote(value);
            }
            return null;
        }
        return "no control called " + quote(name);
    }

    /** The usage, written from the requirements and the three sets above. */
    static String usage() {
        StringBuilder s = new StringBuilder();
        s.append("NAME\n")
                .append("     simRun -- run an OpMode on a simulated robot, or check gamepads\n\n")
                .append("SYNOPSIS\n")
                .append("     ./gradlew :TeamCode:simRun --args=\"ARGUMENTS\"\n\n")
                .append("     ARGUMENTS is one of:\n\n")
                .append("          --pad-check\n")
                .append("          OpModeClass\n")
                .append("          OpModeClass --pad\n")
                .append("          OpModeClass --control=value ...\n\n")
                .append("OPERANDS\n")
                .append("     OpModeClass\n")
                .append("             The OpMode to run, named by the part of its class name that\n")
                .append("             follows ").append(packageName()).append(":\n")
                .append("             lessons.L2aSticksOpMode, example.DriveForward24OpMode.\n\n")
                .append("             It runs on this computer against a simulated robot, a WPILOG\n")
                .append("             is opened for it, and a NetworkTables port is served so\n")
                .append("             AdvantageScope can plot the values while the run is going.\n\n")
                .append("             On its own, every control of both gamepad objects stays at\n")
                .append("             the control's untouched value.\n\n")
                .append("OPTIONS\n")
                .append("     --pad-check\n")
                .append("             Check gamepads and nothing else: no OpMode runs, no simulated\n")
                .append("             robot is started, no WPILOG is opened and no NetworkTables\n")
                .append("             port is served, so this may run beside a simRun that is\n")
                .append("             running an OpMode.\n\n")
                .append("             Prints one line per gamepad naming what was seen, its serial\n")
                .append("             number, its device id and which slot it feeds, and a line\n")
                .append("             when a gamepad arrives or leaves. While a stick is off\n")
                .append("             centre, a trigger is squeezed or a button is held, it prints\n")
                .append("             the gamepad, the slot, the control and its value. Let go of\n")
                .append("             everything and it says nothing is touched.\n\n")
                .append("             Takes the gestures below, and writes the slot to\n")
                .append("             local.properties.\n\n")
                .append("     --pad   Fill the gamepad1 and gamepad2 objects from real gamepads\n")
                .append("             every loop, from the slots named in local.properties. Plug a\n")
                .append("             gamepad in before the run or during it, and unplug it\n")
                .append("             whenever; a gamepad object with no gamepad in its slot reads\n")
                .append("             as untouched. Hold Start and press A to drive as gamepad1,\n")
                .append("             Start and B for gamepad2.\n\n")
                .append("     --help  Print this.\n\n")
                .append("     A control option names one control of the gamepad1 object, spelled\n")
                .append("     as the FTC Gamepad class spells the field a lesson reads. It is set\n")
                .append("     before the OpMode starts and held for the whole run; every control\n")
                .append("     not named keeps its untouched value. Repeatable, and it cannot be\n")
                .append("     combined with --pad.\n\n")
                .append("     A stick axis is -1 to 1, and on both sticks y is negative forward\n")
                .append("     and x is positive right, so --left_stick_y=-1 is the left stick\n")
                .append("     pushed all the way forward. A trigger is 0 at rest to 1 squeezed. A\n")
                .append("     button is true or false, and true is held for the whole run.\n\n")
                .append("FIELDS\n")
                .append("     Settable, and these ").append(CONTROLS.size())
                .append(" are every control a gamepad has:\n");
        wrap(s, AXES);
        wrap(s, BUTTONS);
        s.append("\n     Filled by the SDK from those, correct to read and not settable, along\n")
                .append("     with the 50 aWasPressed and dpadUpWasReleased methods:\n");
        wrap(s, DERIVED);
        s.append("\n     Not supported, and left at an untouched gamepad's values:\n");
        wrap(s, TOUCHPAD);
        return s.toString();
    }

    /** One group of field names, wrapped to the width the rest of the text uses. */
    private static void wrap(StringBuilder s, Set<String> names) {
        StringBuilder line = new StringBuilder("         ");
        for (String name : names) {
            if (line.length() + name.length() + 2 > 79) {
                end(s, line);
                line = new StringBuilder("         ");
            }
            line.append(name).append("  ");
        }
        end(s, line);
    }

    /** One line of the usage, with the padding that ran off the end taken off. */
    private static void end(StringBuilder s, StringBuilder line) {
        s.append(line.toString().replaceAll("\\s+$", "")).append('\n');
    }

    /** The package an OpMode name follows, without the dot that joins them. */
    static String packageName() {
        return SimRun.PACKAGE.substring(0, SimRun.PACKAGE.length() - 1);
    }

    private static Plan bad(String what) {
        return new Plan(null, null, empty(), "simRun: " + what + "; see --help");
    }

    private static List<String> empty() {
        return new ArrayList<>();
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private static boolean isNumber(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Set<String> set(String... names) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(names)));
    }

    private static Set<String> both(Set<String> first, Set<String> second) {
        Set<String> all = new LinkedHashSet<>(first);
        all.addAll(second);
        return Collections.unmodifiableSet(all);
    }
}
