package org.firstinspires.ftc.teamcode.sysid;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.WheelVelocities;

/**
 * Measures what the Control Hub's battery reading is actually worth: how long it
 * costs to read, how often it changes, how finely it resolves, and how fast it
 * follows a real load.
 *
 * <p>Worth measuring because the SDK source settles less than you would hope.
 * {@code LynxVoltageSensor.getVoltage()} sends one ADC command to the hub and
 * returns the reply in millivolts -- no smoothing, no history, no caching, and
 * notably <b>not part of the bulk read</b>, so every call is its own round trip.
 * Whether the hub's own firmware or its analogue front end filters the signal is
 * not published anywhere, and claims about it online trace back to a different
 * vendor's motor controller. So: measure.
 *
 * <p><b>Put the robot on blocks.</b> The wheels spin freely and the test drives
 * them in square waves; on the floor it would drive off.
 *
 * <p>Hold the right trigger. The test steps through its phases by itself and
 * stops at the end. Download the log afterwards.
 *
 * <table>
 *   <tr><td>phase 0</td><td>idle, 3 s</td><td>resolution and call cost with no load</td></tr>
 *   <tr><td>1 to 5</td><td>square wave at 1, 2, 5, 10, 20 Hz</td><td>how far the reading swings as the load gets faster</td></tr>
 * </table>
 *
 * <p>What the numbers mean afterwards:
 * <ul>
 *   <li><b>call_us</b> -- what one reading costs. If it is large, read it less
 *       often than every loop.</li>
 *   <li><b>volts</b> at idle -- the distinct values it takes are its resolution,
 *       and how often it changes is its update rate.</li>
 *   <li><b>swing per frequency</b> -- the amplitude of the voltage wobble
 *       against drive frequency. Where it falls to about 70% of the 1 Hz value
 *       is the corner frequency of whatever filtering exists, hardware or
 *       firmware. If it barely falls by 20 Hz, there is little filtering and the
 *       reading can be trusted per loop.</li>
 * </ul>
 */
@TeleOp(name = "SysId: voltage response", group = "SysId")
public class VoltageResponseOpMode extends CorbelsTeleOp {

    /** The square wave frequencies, in hertz. Phase 0 is idle. */
    public static final double[] FREQUENCIES = {0, 1, 2, 5, 10, 20};

    /** How long each phase lasts, in seconds. */
    public static final double PHASE_SECONDS = 3.0;

    /** How hard the square wave drives. Enough to move the rail, not the robot. */
    static final double AMPLITUDE = 0.4;

    private CorbelsMecanum drivetrain;
    private WheelVelocities wheels;
    private boolean running;
    private long startNs;

    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void start() {
        startBefore();
        wheels = new WheelVelocities(hardware);
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
        boolean wanted = gamepad1.right_trigger > 0.5;
        if (wanted && !running) {
            running = true;
            startNs = System.nanoTime();
        } else if (!wanted) {
            running = false;
        }

        double seconds = running ? (System.nanoTime() - startNs) / 1e9 : 0;
        int phase = phaseAt(seconds);
        boolean finished = running && phase >= FREQUENCIES.length;
        double power = finished ? 0 : powerAt(phase, seconds, AMPLITUDE);
        if (!running) power = 0;

        // Time the reading itself: this is the number that decides whether it
        // can be afforded every loop.
        long before = System.nanoTime();
        double volts = hardware.batteryVolts();
        double callMicros = (System.nanoTime() - before) / 1000.0;

        drivetrain.setCommandedWheels(power, power, power, power);

        Tracker.publish("volts", volts);
        Tracker.publish("volts/call_us", callMicros);
        Tracker.publish("drive/power", power);
        Tracker.publish("drive/phase", phase);
        Tracker.publish("drive/frequency_hz", finished ? 0 : FREQUENCIES[Math.min(phase, FREQUENCIES.length - 1)]);
        Tracker.publish("drive/wheel_ips", wheels.frontLeftInchesPerSecond());
        Tracker.publish("running", running && !finished);

        Tracker.printToDs("State  "
                + (running ? (finished ? "DONE" : "phase " + phase) : "hold the right trigger"));
        if (running && !finished) {
            Tracker.printToDs("Frequency  %.0f Hz", FREQUENCIES[phase]);
            Tracker.printToDs("Phase ends in  %.1f s", (phase + 1) * PHASE_SECONDS - seconds);
        }
        Tracker.printToDs("Volts  %.3f   (read took %.0f us)", volts, callMicros);
        Tracker.printToDs("Robot on blocks. Total run: "
                + (int) (FREQUENCIES.length * PHASE_SECONDS) + " seconds.");

        loopAfter();
    }

    /** Which phase we are in, this far into the run. */
    public static int phaseAt(double seconds) {
        return (int) (seconds / PHASE_SECONDS);
    }

    /**
     * The square wave for a phase: zero while idle, otherwise alternating
     * between plus and minus the amplitude at that phase's frequency.
     */
    public static double powerAt(int phase, double seconds, double amplitude) {
        if (phase < 0 || phase >= FREQUENCIES.length) return 0;
        double frequency = FREQUENCIES[phase];
        if (frequency == 0) return 0;
        double intoPhase = seconds - phase * PHASE_SECONDS;
        // half a period high, half low
        boolean high = (intoPhase * frequency) % 1.0 < 0.5;
        return high ? amplitude : -amplitude;
    }

    @Override
    protected void afterLoop() {
        if (!running) drivetrain.setCommandedWheels(0, 0, 0, 0);
    }
}
