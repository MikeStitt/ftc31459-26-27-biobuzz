package org.firstinspires.ftc.teamcode.sysid;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.SysIdRecorder;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.WheelVelocities;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Collects the data WPILib's SysId analyser needs, for the drivetrain.
 *
 * <p>Four tests, one file. Pick a test with the D-pad, hold the right trigger to
 * run it, release to stop. Between tests nothing is recorded, so all four runs
 * can live in one log -- which is what the analyser expects.
 *
 * <table>
 *   <tr><td>D-pad up</td><td>quasistatic forward -- a slow voltage ramp</td></tr>
 *   <tr><td>D-pad down</td><td>quasistatic reverse</td></tr>
 *   <tr><td>D-pad right</td><td>dynamic forward -- a step to {@link #STEP_VOLTS}</td></tr>
 *   <tr><td>D-pad left</td><td>dynamic reverse</td></tr>
 * </table>
 *
 * <p><b>Space.</b> The quasistatic runs need three or four metres; release the
 * trigger before the wall. The dynamic runs take about a second.
 *
 * <p><b>Afterwards.</b> Download the log from
 * {@code 192.168.43.1:8080/corbelsflightlog}, open it in SysId, choose the
 * {@code sysid-test-state-drive} channel as Test State and one motor's three
 * channels for voltage, position and velocity, and set the units to
 * <b>Inches</b>.
 */
@TeleOp(name = "SysId: drivetrain", group = "SysId")
public class SysIdDriveOpMode extends CorbelsTeleOp {

    /** How fast the quasistatic test raises the voltage. Volts per second. */
    private static final double RAMP_VOLTS_PER_SECOND = 0.25;

    /** What the dynamic test steps to. Volts. */
    private static final double STEP_VOLTS = 4.0;

    private static final String[] MOTOR_NAMES = {"frontLeft", "frontRight", "backLeft", "backRight"};

    private CorbelsMecanum drivetrain;
    private SysIdRecorder recorder;
    private WheelVelocities wheels;

    private SysIdRecorder.State selected = SysIdRecorder.State.QUASISTATIC_FORWARD;
    private boolean running;
    private long startNs;
    private double[] startTicks;

    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void start() {
        startBefore();
        recorder = new SysIdRecorder(Tracker.flightlog, "drive");
        wheels = new WheelVelocities(hardware);
        startTicks = ticks();
        startAfter();
    }

    @Override
    public void stop() {
        drivetrain.releaseCommandedWheels();
        stopAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        if (!running) {
            if (gamepad1.dpad_up) selected = SysIdRecorder.State.QUASISTATIC_FORWARD;
            if (gamepad1.dpad_down) selected = SysIdRecorder.State.QUASISTATIC_REVERSE;
            if (gamepad1.dpad_right) selected = SysIdRecorder.State.DYNAMIC_FORWARD;
            if (gamepad1.dpad_left) selected = SysIdRecorder.State.DYNAMIC_REVERSE;
        }

        boolean wanted = gamepad1.right_trigger > 0.5;
        if (wanted && !running) {
            running = true;
            startNs = System.nanoTime();
            startTicks = ticks();
        } else if (!wanted && running) {
            running = false;
            recorder.state(SysIdRecorder.State.NONE);
        }

        double volts = 0;
        if (running) {
            double seconds = (System.nanoTime() - startNs) / 1e9;
            volts = voltsFor(selected, seconds, RAMP_VOLTS_PER_SECOND, STEP_VOLTS);
        }

        double battery = hardware.batteryVolts();
        double power = battery > 1 ? clamp(volts / battery) : 0;
        drivetrain.setCommandedWheels(power, power, power, power);

        if (running) {
            recorder.state(selected);
            double[] now = ticks();
            double[] speeds = wheels.all();
            for (int i = 0; i < 4; i++) {
                recorder.motor(MOTOR_NAMES[i], power, battery,
                        (now[i] - startTicks[i]) / Constants.ticksPerInch, speeds[i]);
            }
        }

        Tracker.publish("sysid/running", running);
        Tracker.publish("sysid/test", selected.toString());
        Tracker.publish("sysid/volts_commanded", volts);
        Tracker.publish("sysid/power", power);
        Tracker.publish("sysid/battery_volts", battery);

        Tracker.printToDs("Test  %s   %s", selected, running ? "RUNNING" : "ready");
        Tracker.printToDs("D-pad picks the test. Hold the right trigger to run it.");
        Tracker.printToDs("Volts  %.2f of %.1f available", volts, battery);
        Tracker.printToDs("Up: quasi fwd   Down: quasi rev   Right: dyn fwd   Left: dyn rev");

        loopAfter();
    }

    /** What the voltage should be, this far into the given test. */
    public static double voltsFor(SysIdRecorder.State test, double seconds,
                           double rampVoltsPerSecond, double stepVolts) {
        switch (test) {
            case QUASISTATIC_FORWARD:
                return rampVoltsPerSecond * seconds;
            case QUASISTATIC_REVERSE:
                return -rampVoltsPerSecond * seconds;
            case DYNAMIC_FORWARD:
                return stepVolts;
            case DYNAMIC_REVERSE:
                return -stepVolts;
            default:
                return 0;
        }
    }

    private double[] ticks() {
        return new double[]{
                hardware.frontLeft.getCurrentPosition(), hardware.frontRight.getCurrentPosition(),
                hardware.backLeft.getCurrentPosition(), hardware.backRight.getCurrentPosition()};
    }

    private static double clamp(double v) {
        return Math.max(-1, Math.min(1, v));
    }

    @Override
    protected void afterLoop() {
        if (!running) drivetrain.setCommandedWheels(0, 0, 0, 0);
    }
}
