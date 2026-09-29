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
 * <p>Everything periodic runs on {@link SimTicker}'s 10 ms grid, measured from
 * the clock read when the run starts: the gamepads are read and the simulated
 * robot steps every instant it wakes on, the gestures are read every fifth
 * instant, and the rescan runs every twenty-fifth with the off-rest report
 * beside it for {@code --pad-check}. A slow pass is charged to itself rather
 * than to the periods after it, and an instant it ran through is skipped rather
 * than caught up.
 *
 * <p>So one pass is 10 ms of simulated time, but not necessarily 10 ms of real
 * time. Measured on this bench on 2026-09-28: {@code Thread.sleep(10)} takes
 * 13.4 ms, so the loop wakes on every second instant and the robot moves at
 * about half the speed it would on the field. The work in a pass is 0.03 ms of
 * that, so it is the waiting and not the simulating.
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
        OpModeHarness harness = null;
        if (plan.form != SimArgs.Form.PAD_CHECK) {
            OpMode opMode;
            try {
                opMode = opMode(plan.opMode);
            } catch (ReflectiveOperationException | ClassCastException e) {
                System.err.println("simRun: no OpMode called \"" + plan.opMode + "\" under "
                        + SimArgs.packageName() + "; see --help");
                System.exit(2);
                return;
            }
            harness = new OpModeHarness(opMode);
        }
        Gamepad typed = null;
        for (String control : plan.controls) {
            if (typed == null) {
                typed = new Gamepad();
            }
            set(typed, control);
        }
        loop(plan, harness, typed);
    }

    /**
     * One loop for both forms, over the instants on {@link SimTicker}'s grid.
     *
     * <p>{@code --pad-check} is this loop with the simulated robot, the WPILOG
     * and the NetworkTables server left out, which is what makes the slots, the
     * gestures and the rescan one implementation rather than two. Its two gamepad
     * objects are real ones that no lesson reads.
     *
     * <p>Every instant reads the gamepads first, so everything due at that
     * instant reads the same state. Then whatever is due runs, and the pass waits
     * for the next instant rather than for a fixed time after itself.
     */
    private static void loop(SimArgs.Plan plan, OpModeHarness harness, Gamepad typed) {
        Gamepad slot1 = harness == null ? new Gamepad() : harness.gamepad1;
        Gamepad slot2 = harness == null ? new Gamepad() : harness.gamepad2;
        boolean usePads = plan.form == SimArgs.Form.OPMODE_PAD
                || plan.form == SimArgs.Form.PAD_CHECK;

        try (SimPublisher out = harness == null ? null : new SimPublisher(harness);
                SimPads pads = usePads ? SimPads.open() : null) {
            if (harness != null) {
                System.out.println(plan.opMode + " running. Connect AdvantageScope to 127.0.0.1"
                        + " as NetworkTables 4, and Ctrl-C to stop.");
                harness.init();
                harness.start();
            }
            long origin = System.currentTimeMillis();
            long tick = 0;
            long lastStep = -1;
            long lastGestures = -1;
            long lastReport = -1;
            while (running) {
                if (pads != null) {
                    pads.read(slot1, slot2);
                }
                if (typed != null) {
                    slot1.copy(typed);
                }
                if (SimTicker.due(tick, lastStep, SimTicker.STEP_TICKS)) {
                    lastStep = tick;
                    if (harness != null) {
                        harness.loop();
                        out.publish();
                    }
                }
                if (pads != null && SimTicker.due(tick, lastGestures, SimTicker.GESTURE_TICKS)) {
                    lastGestures = tick;
                    pads.gestures();
                }
                if (pads != null && SimTicker.due(tick, lastReport, SimTicker.REPORT_TICKS)) {
                    lastReport = tick;
                    pads.rescan();
                    if (plan.form == SimArgs.Form.PAD_CHECK) {
                        pads.report();
                    }
                }
                OpModeHarness.sleep(SimTicker.startOf(origin, tick + 1)
                        - System.currentTimeMillis());
                tick = SimTicker.tickAt(origin, System.currentTimeMillis());
            }
            if (harness != null) {
                harness.stop();
            }
        }
        if (harness != null) {
            System.out.println("Flight logs: " + harness.logFolder);
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
