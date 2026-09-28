package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs one lesson in the simulator until Ctrl-C, publishing to
 * NetworkTables every loop so AdvantageScope can watch it.
 *
 * <p>{@code ./gradlew :TeamCode:simRun} with no arguments runs
 * {@code L15CombinedOpMode} standing still. Arguments name a lesson and then set
 * gamepad fields by their own names:
 *
 * <pre>
 * ./gradlew :TeamCode:simRun --args="L2bTankOpMode left_stick_y=-1 right_stick_y=-1"
 * </pre>
 *
 * <p>Any field of {@code Gamepad} can be set, so buttons work the same way as
 * sticks: {@code a=true}. The values are set once, before the lesson starts,
 * and held.
 *
 * <p>The word {@code pad} on its own reads a real gamepad every loop instead:
 *
 * <pre>
 * ./gradlew :TeamCode:simRun --args="L15CombinedOpMode pad"
 * </pre>
 *
 * <p>{@link SimPads} says which pad is which, and a pad plugged in after the run
 * starts is picked up. Without {@code pad} nothing opens SDL, so a run with no
 * gamepad on the machine behaves exactly as it did.
 *
 * <p>Not both. Every argument that is not {@code pad} sets a field of
 * {@code gamepad1}, which the pad would overwrite on its first loop, so the two
 * together are invalid parameters: one line on stderr and exit 2.
 *
 * <p>Each loop is {@code stepMs} of simulated time and sleeps the same in real
 * time, so the robot moves at about the speed it would on the field.
 *
 * <p>Passes when: SimRunTest.
 */
public final class SimRun {

    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        String bad = invalid(args);
        if (bad != null) {
            System.err.println(bad);
            System.exit(2);
        }
        String lesson = args.length > 0 ? args[0] : "L15CombinedOpMode";
        OpModeHarness harness = new OpModeHarness(lesson(lesson));
        boolean readPads = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("pad")) {
                readPads = true;
            } else {
                set(harness, args[i]);
            }
        }

        Thread loopThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                loopThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        try (SimPublisher out = new SimPublisher(harness);
                SimPads pads = readPads ? SimPads.open() : null) {
            System.out.println(lesson + " running. Connect AdvantageScope to 127.0.0.1"
                    + " as NetworkTables 4, and Ctrl-C to stop.");
            harness.init();
            harness.start();
            while (running) {
                if (pads != null) {
                    pads.update(harness.gamepad1, harness.gamepad2);
                }
                harness.loop();
                out.publish();
                OpModeHarness.sleep(harness.stepMs);
            }
            harness.stop();
        }
        System.out.println("Flight logs: " + harness.logFolder);
    }

    /** One lesson by its class name, with no package. */
    static OpMode lesson(String name) throws ReflectiveOperationException {
        Class<?> type = Class.forName("org.firstinspires.ftc.teamcode.lessons." + name);
        return (OpMode) type.getDeclaredConstructor().newInstance();
    }

    /** One {@code name=value} onto {@code gamepad1}, by the field's own name. */
    /**
     * Why these arguments cannot be used together, or null if they can.
     *
     * <p>Only the bare word {@code pad} is the flag, so {@code pad=true} counts
     * as a setting and fails in {@link #set} on its own name.
     */
    static String invalid(String[] args) {
        boolean pad = false;
        List<String> settings = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("pad")) {
                pad = true;
            } else {
                settings.add(args[i]);
            }
        }
        if (!pad || settings.isEmpty()) {
            return null;
        }
        return "invalid arguments: \"pad\" cannot be combined with gamepad settings; got "
                + String.join(", ", settings);
    }

    static void set(OpModeHarness harness, String assignment) throws ReflectiveOperationException {
        int equals = assignment.indexOf('=');
        if (equals < 1) {
            throw new IllegalArgumentException(
                    "expected a gamepad field as name=value, got \"" + assignment + "\"");
        }
        String name = assignment.substring(0, equals);
        String value = assignment.substring(equals + 1);
        Field field;
        try {
            field = harness.gamepad1.getClass().getField(name);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("no gamepad field called \"" + name
                    + "\"; the sticks are left_stick_x, left_stick_y, right_stick_x"
                    + " and right_stick_y", e);
        }
        if (field.getType() == float.class) {
            field.setFloat(harness.gamepad1, Float.parseFloat(value));
        } else if (field.getType() == boolean.class) {
            field.setBoolean(harness.gamepad1, Boolean.parseBoolean(value));
        } else {
            throw new IllegalArgumentException(name + " is a " + field.getType().getSimpleName()
                    + ", and only the float and boolean fields can be set");
        }
    }
}
