package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import java.lang.reflect.Field;

/**
 * Runs one OpMode in the simulator until Ctrl-C, publishing to NetworkTables
 * every loop so AdvantageScope can watch it, or checks gamepads and nothing
 * else.
 *
 * <pre>
 * ./gradlew :TeamCode:simRun --args="--help"
 * ./gradlew :TeamCode:simRun --args="--pad-check"
 * ./gradlew :TeamCode:simRun --args="lessons.L2bTankOpMode"
 * ./gradlew :TeamCode:simRun --args="lessons.L2bTankOpMode --pad"
 * ./gradlew :TeamCode:simRun --args="lessons.L2bTankOpMode --left_stick_y=-1"
 * </pre>
 *
 * <p>{@link SimArgs} holds the four forms, the 21 controls that can be set and
 * the usage text; {@code --help} prints it. A usage error is one line on
 * standard error and exit 2, with no stack trace.
 *
 * <p>An OpMode is named by what follows {@link #PACKAGE}, so any OpMode in the
 * project can be run. A control option fills a staging gamepad that is copied
 * into {@code gamepad1} every loop, and {@code --pad} fills both slots from real
 * gamepads instead; {@link SimPads} says which gamepad is which.
 *
 * <p>Each loop is {@code stepMs} of simulated time and sleeps the same in real
 * time, so the robot moves at about the speed it would on the field.
 *
 * <p>Passes when: SimRunTest, SimArgsTest.
 */
public final class SimRun {

    /** The package an OpMode's name is relative to. Written here and nowhere else. */
    static final String PACKAGE = "org.firstinspires.ftc.teamcode.";

    private static volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        SimArgs.Plan plan = SimArgs.parse(args);
        if (plan.error != null) {
            System.err.println(plan.error);
            System.exit(2);
            return;
        }
        if (plan.form == SimArgs.Form.HELP) {
            System.out.println(SimArgs.usage());
            return;
        }
        stopOnCtrlC(Thread.currentThread());
        if (plan.form == SimArgs.Form.PAD_CHECK) {
            padCheck();
            return;
        }
        OpMode opMode;
        try {
            opMode = opMode(plan.opMode);
        } catch (ReflectiveOperationException | ClassCastException e) {
            System.err.println("simRun: no OpMode called \"" + plan.opMode + "\" under "
                    + SimArgs.packageName() + "; see --help");
            System.exit(2);
            return;
        }
        OpModeHarness harness = new OpModeHarness(opMode);
        Gamepad typed = null;
        for (String control : plan.controls) {
            if (typed == null) {
                typed = new Gamepad();
            }
            set(typed, control);
        }
        boolean readPads = plan.form == SimArgs.Form.OPMODE_PAD;

        try (SimPublisher out = new SimPublisher(harness);
                SimPads pads = readPads ? SimPads.open() : null) {
            System.out.println(plan.opMode + " running. Connect AdvantageScope to 127.0.0.1"
                    + " as NetworkTables 4, and Ctrl-C to stop.");
            harness.init();
            harness.start();
            while (running) {
                if (pads != null) {
                    pads.update(harness.gamepad1, harness.gamepad2);
                }
                if (typed != null) {
                    harness.gamepad1.copy(typed);
                }
                harness.loop();
                out.publish();
                OpModeHarness.sleep(harness.stepMs);
            }
            harness.stop();
        }
        System.out.println("Flight logs: " + harness.logFolder);
    }

    /**
     * Gamepads and nothing else, until Ctrl-C.
     *
     * <p>No OpMode, no simulated robot, no WPILOG and no NetworkTables port, so
     * this runs beside a {@code simRun} that is running an OpMode. The two
     * gamepad objects are real ones that no lesson reads, filled the same way a
     * lesson's are, which is what makes the slots and the gestures the same
     * rules here as there.
     */
    private static void padCheck() {
        Gamepad slot1 = new Gamepad();
        Gamepad slot2 = new Gamepad();
        try (SimPads pads = SimPads.open()) {
            while (running) {
                pads.update(slot1, slot2);
                OpModeHarness.sleep(10);
            }
        }
    }

    /** Ends the loop on Ctrl-C, so a try-with-resources still closes. */
    private static void stopOnCtrlC(Thread loopThread) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                loopThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    /** One OpMode by the part of its class name that follows {@link #PACKAGE}. */
    static OpMode opMode(String name) throws ReflectiveOperationException {
        Class<?> type = Class.forName(PACKAGE + name);
        return (OpMode) type.getDeclaredConstructor().newInstance();
    }

    /**
     * One {@code name=value} onto {@code into}, by the field's own name.
     *
     * <p>{@code into} is the pad the arguments are collected in, which is copied
     * into {@code gamepad1} every loop rather than written there once. So a
     * typed control and a real gamepad reach a lesson by the same path, and the
     * SDK derives the aliases and the edges from both.
     *
     * <p>Which names are controls is {@link SimArgs}'s business, and it has said
     * so before this is called.
     */
    static void set(Gamepad into, String assignment) throws ReflectiveOperationException {
        int equals = assignment.indexOf('=');
        String name = assignment.substring(0, equals);
        String value = assignment.substring(equals + 1);
        Field field = into.getClass().getField(name);
        if (field.getType() == float.class) {
            field.setFloat(into, Float.parseFloat(value));
        } else {
            field.setBoolean(into, Boolean.parseBoolean(value));
        }
    }
}
