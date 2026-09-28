package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.lang.reflect.Field;

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
 * and held. Driving the sticks while it runs is
 * {@code sim.sticks.input} in {@code open-work.md}, and is not built.
 *
 * <p>Each loop is {@code stepMs} of simulated time and sleeps the same in real
 * time, so the robot moves at about the speed it would on the field.
 *
 * <p>Passes when: SimRunTest.
 */
public final class SimRun {

    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        String lesson = args.length > 0 ? args[0] : "L15CombinedOpMode";
        OpModeHarness harness = new OpModeHarness(lesson(lesson));
        for (int i = 1; i < args.length; i++) {
            set(harness, args[i]);
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

        try (SimPublisher out = new SimPublisher(harness)) {
            System.out.println(lesson + " running. Connect AdvantageScope to 127.0.0.1"
                    + " as NetworkTables 4, and Ctrl-C to stop.");
            harness.init();
            harness.start();
            while (running) {
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
