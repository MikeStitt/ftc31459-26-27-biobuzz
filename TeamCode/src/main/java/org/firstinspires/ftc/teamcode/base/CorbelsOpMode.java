package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.OpModeStorage;


/**
 * What every Corbels OpMode does, whether a driver is holding the controller or
 * not: find the hardware, make the follower, run the scheduler, keep Panels and
 * the Driver Station fed.
 *
 * <p>TeleOps and autos differ in three places, and those are the hooks below:
 * {@link #onInit}, {@link #onStart} and {@link #afterLoop}. {@link CorbelsTeleOp}
 * and {@link CorbelsAuto} fill them in; a lesson extends one of those, not this.
 *
 * <p>Logging lives in {@link Tracker}, which any class can reach:
 * {@code Tracker.publish} for a number, {@code Tracker.printToDs} for the
 * driver's screen. Every run writes a WPILOG file, openable in AdvantageScope
 * afterwards: everything published, the robot's pose and path from Pedro, and
 * the shadow localizers. It opens at init, so setting-up values are in it as
 * well as the run. Files land in {@code /sdcard/corbelsflightlog} and can be
 * downloaded from {@code http://192.168.43.1:8080/corbelsflightlog}.
 *
 * <p>A lesson writes {@code init}, {@code start}, {@code loop} and {@code stop}
 * itself, the way every FTC example does, and calls the hooks below from inside
 * them: {@link #initBefore} and {@link #initAfter}, {@link #startBefore} and
 * {@link #startAfter}, {@link #loopBefore} and {@link #loopAfter}, and
 * {@link #stopAfter}. The hooks are final; what goes between them is the
 * lesson's.
 *
 * <p>{@code init} and {@code loop} are not written here, so the compiler asks
 * every lesson for them, which is what the SDK does too. {@code start} and
 * {@code stop} have the obvious bodies below, and a lesson with nothing of its
 * own to do there may leave them alone.
 */
public abstract class CorbelsOpMode extends OpMode {

    /** Every device on the robot, resolved in init(). */
    protected RobotHardware hardware;

    protected Follower follower;

    /**
     * The drivetrain handed to {@link #initAfter(Drivetrain)}, kept so that
     * {@link #stopAfter} can stop it. A lesson that drives its own wheels passes
     * none, and gets the {@link PassiveDriveTrain} that {@link #initAfter()}
     * makes, whose {@code stop()} does nothing.
     */
    private Drivetrain heldDrivetrain;

    protected ShadowLocalizers shadowLocalizers;

    // ------------------------------------------------------------ hooks

    protected abstract boolean isAuto();

    /** After the hardware and follower exist, before the first update. */
    protected void onInit() {
    }

    /** Once, when the OpMode starts, before {@link #shadowLocalizers()}. */
    protected void onStart() {
    }

    /** Every loop, after the scheduler and the shadow localizers. */
    protected void afterLoop() {
    }

    /** Extra localizers to watch. Runs once, when the OpMode starts. */
    protected void shadowLocalizers() {
    }

    // ------------------------------------------------------------ logging

    protected static String describe(Pose p) {
        return String.format("x %.1f  y %.1f  h %.0f deg", p.x(), p.y(), Math.toDegrees(p.heading()));
    }

    // ------------------------------------------------------------ lifecycle

    /** The hardware and the flight log. The first thing a lesson's init() calls. */
    protected final void initBefore() {
        // Every device, looked up once, before the match starts. A name that
        // doesn't match the configuration fails here, where it can be read --
        // not halfway through a match.
        hardware = RobotFactory.hardware.apply(hardwareMap);
        // From init, so anything logged while setting up -- a starting pose, a
        // sensor reading, a configuration problem -- is in the file too. The
        // Robot Controller closes it if the OpMode never runs.
        Tracker.begin(this);
    }

    /**
     * The follower, for a lesson that sets the motors itself. The follower reads
     * the localizer and reports the pose, and never touches a motor.
     */
    protected final void initAfter() {
        initAfter(new PassiveDriveTrain());
    }

    /** The drivetrain this OpMode drives, as it was handed to {@link #initAfter}. */
    public final Drivetrain drivetrain() {
        return heldDrivetrain;
    }

    /** The follower, driving the drivetrain given. The last thing a lesson's init() calls. */
    protected final void initAfter(Drivetrain heldDrivetrain) {
        this.heldDrivetrain = heldDrivetrain;
        follower = RobotFactory.follower.apply(hardwareMap, heldDrivetrain);
        if (!isAuto()) {
            follower.setPose(OpModeStorage.autonomousEndPose);
        }
        onInit();
        follower.update();
        Tracker.printToDs("Panels: http://192.168.43.1:8001");
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    /** The scheduler, Panels and the logger. The first thing a lesson's start() calls. */
    protected final void startBefore() {
        Scheduler.reset();
        Tracker.startLogging();
        follower = follower.withLogger(followerLog -> Tracker.logger.pedro(followerLog.toString()));
        shadowLocalizers = new ShadowLocalizers();
        onStart();
    }

    /** The shadow localizers. The last thing a lesson's start() calls. */
    protected final void startAfter() {
        shadowLocalizers();
        shadowLocalizers.setPose(follower.pose());
        // The live localizer, under a name, so a lesson's own localizer can be
        // graphed against it rather than against the bare robot pose. Published
        // rather than driven: the follower already updates it every loop.
        shadowLocalizers.publishOnly("pinPoint", follower.localizer);
    }

    /**
     * Reads the gamepads. The first thing a lesson's loop() calls, so a button
     * pressed now is seen by the lesson's own code now.
     */
    protected final void loopBefore() {
        pollInputs();
    }

    /** Where {@link CorbelsTeleOp} reads its buttons. Nothing for an auto to do. */
    protected void pollInputs() {
    }

    /** The follower, the scheduler and the logs. The last thing a lesson's loop() calls. */
    protected final void loopAfter() {
        follower.update();
        Scheduler.execute();
        shadowLocalizers.update();
        afterLoop();
        // Counts the loop, records Pedro, closes the file's record for this loop,
        // then flushes Panels and the Driver Station.
        Tracker.endLoop(follower);
    }

    @Override
    public void stop() {
        stopAfter();
    }

    /** Wheels to zero and the flight log closed. The last thing a lesson's stop() calls. */
    protected final void stopAfter() {
        follower.manual(0, 0, 0);
        follower.update();
        if (isAuto()) {
            OpModeStorage.autonomousEndPose = follower.pose();
        }
        // The drivetrain, not the follower: a lesson that commanded the wheels
        // has them ignoring the follower, so the zero above never reaches a
        // motor. stop() hands them back and writes four zeros.
        heldDrivetrain.stop();
        Tracker.close();
    }
}
