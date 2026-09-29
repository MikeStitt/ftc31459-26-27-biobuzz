package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Which pad is {@code gamepad1} and which is {@code gamepad2}.
 *
 * <p>{@link SimGamepad} reads pads; this decides whose readings go where, the
 * way the Driver Station does: hold Start and press A to be {@code gamepad1},
 * Start and B to be {@code gamepad2}.
 *
 * <p>One pad skips the gesture and becomes {@code gamepad1}. Two pads and no
 * assignment means the lesson reads a resting {@code Gamepad} and the robot
 * sits still until somebody claims one; the run is not refused. An assignment is
 * remembered in {@code local.properties}, which is per-machine and gitignored
 * already, keyed on each pad's serial.
 *
 * <p>Passes when: SimPadsTest.
 */
public final class SimPads implements AutoCloseable {

    static final String KEY1 = "sim.gamepad1.serial";

    static final String KEY2 = "sim.gamepad2.serial";

    /**
     * How a pad in a slot got there. Remembered, because it cannot be derived.
     */
    enum Source {
        /** The only gamepad plugged in, so no gesture was asked for. */
        ONLY_PAD,
        /** Read back from {@code local.properties} by serial number. */
        STORED,
        /** Claimed with Start and A, or Start and B, during this run. */
        GESTURE
    }

    /**
     * What a gamepad the library is listing is doing, which is what its census
     * line says.
     *
     * <p>Derived rather than remembered: {@link #state} is a pure function of the
     * gamepads plugged in and the two slots, so a gamepad's state can change
     * because another gamepad moved.
     */
    enum State {
        /** No serial number, so it is not read and no slot takes it. */
        NOT_ACCEPTED,
        /** Another gamepad reports the same serial number with a higher id. */
        PASSED_OVER,
        /** Filling {@code gamepad1} or {@code gamepad2}. */
        ACTIVE,
        /** No slot yet, and one is free for a gesture to fill. */
        UNCLAIMED_FREE,
        /** No slot, and both are taken by other gamepads. */
        UNCLAIMED_FULL
    }

    /**
     * Which of the five states a gamepad is in.
     *
     * <p>Pure, and the order matters: a gamepad holding a slot keeps it, so
     * {@code ACTIVE} is asked first and a twin arriving with a higher device id
     * does not pass an active gamepad over.
     */
    static State state(SimGamepad.Pad pad, List<SimGamepad.Pad> pads,
            SimGamepad.Pad player1, SimGamepad.Pad player2) {
        if (pad == player1 || pad == player2) {
            return State.ACTIVE;
        }
        if (!accepted(pad)) {
            return State.NOT_ACCEPTED;
        }
        if (passedOver(pad, pads)) {
            return State.PASSED_OVER;
        }
        return player1 == null || player2 == null
                ? State.UNCLAIMED_FREE
                : State.UNCLAIMED_FULL;
    }

    private final SimGamepad sdl;

    private final Path store;

    private final PrintStream out;

    /** Scratch, so a pad can be read for its gesture without reaching a lesson. */
    private final Gamepad probe = new Gamepad();

    /**
     * Where a pad's reading is held before it is copied, one per slot.
     *
     * <p>A reading goes into one of these and is then handed to the slot's own
     * {@code Gamepad.copy}, which is how the robot fills a gamepad: the nine
     * derived fields and every {@code WasPressed} method come out of that call
     * rather than out of this code. Held rather than made each loop, so a pass
     * allocates nothing.
     */
    private final Gamepad staging1 = new Gamepad();

    private final Gamepad staging2 = new Gamepad();

    private final Map<SimGamepad.Pad, Source> source = new LinkedHashMap<>();

    private SimGamepad.Pad player1;

    private SimGamepad.Pad player2;

    /**
     * The off-rest report printed last, so a stick held still prints once.
     *
     * <p>Empty before the first one, which is why a run where nothing has been
     * touched yet does not say so.
     */
    private String lastReport = "";

    /**
     * The census text printed last, so an unchanged census prints nothing.
     *
     * <p>Empty before the first one, which is why a run with nothing plugged in
     * still says so.
     */
    private String lastCensus = "";

    SimPads(SimGamepad sdl, Path store, PrintStream out) {
        this.sdl = sdl;
        this.store = store;
        this.out = out;
        assign();
        census();
    }

    /** Opens SDL, loads any stored assignment, and says what it found. */
    public static SimPads open() {
        return new SimPads(SimGamepad.open(), localProperties(), System.out);
    }

    /**
     * Reads every pad and hands each player's reading to its {@code Gamepad}.
     *
     * <p>Called once per instant on {@link SimTicker}'s grid, before anything
     * else that instant, so every job that runs sees the same reading. A
     * {@code Gamepad} with no pad behind it reads as untouched.
     */
    public void read(Gamepad gamepad1, Gamepad gamepad2) {
        sdl.update();
        fill(player1, staging1, gamepad1);
        fill(player2, staging2, gamepad2);
    }

    /**
     * Watches every gamepad without a slot for Start and A, or Start and B.
     *
     * <p>Every fifth instant. Faster buys nothing: the gesture is held rather
     * than a press, so 50 ms cannot miss one.
     *
     * <p>A full pair of slots is not a reason to stop watching: a gesture takes
     * an occupied slot and moves whoever was there out of the way. A gamepad
     * already in a slot is watched too, so one gamepad on its own can move
     * itself to the other slot; a gesture that changes nothing is ignored, which
     * is what a held Start and A on the gamepad that already has that slot is.
     */
    public void gestures() {
        List<SimGamepad.Pad> all = sdl.pads();
        for (SimGamepad.Pad pad : all) {
            if (state(pad, all, player1, player2) == State.NOT_ACCEPTED) {
                continue;
            }
            sdl.read(pad, probe);
            int player = claimedPlayer(probe);
            if (player != 0) {
                take(pad, player);
            }
        }
    }

    /**
     * Fills one slot's {@code Gamepad} the way the robot fills it.
     *
     * <p>An empty slot is reset instead, every loop rather than once when the
     * pad went away, so a robot being driven forward stops when the cable comes
     * out and stays stopped.
     */
    private void fill(SimGamepad.Pad pad, Gamepad staging, Gamepad target) {
        if (pad == null) {
            target.reset();
            return;
        }
        sdl.read(pad, staging);
        target.copy(staging);
    }

    @Override
    public void close() {
        sdl.close();
    }

    /**
     * Picks up a pad plugged in since the last look, and lets go of one
     * unplugged.
     *
     * <p>Every twenty-fifth instant, which is four times a second: a rescan
     * costs about 1 us, measured, so the period is politeness to the outlier
     * rather than to the mean, and it is far faster than a hand with a cable.
     *
     * <p>A pad that goes away releases its player, and {@link #read} resets that
     * player's {@code Gamepad} on the next instant. Each event prints its own
     * line and then {@link #census()} says where every gamepad now stands, so a
     * gamepad whose state changed because another one moved shows up too.
     */
    public void rescan() {
        for (SimGamepad.Pad pad : sdl.departed()) {
            source.remove(pad);
            String left = "was not claimed";
            if (pad == player1) {
                player1 = null;
                left = "gamepad1 is back to rest";
            } else if (pad == player2) {
                player2 = null;
                left = "gamepad2 is back to rest";
            }
            out.println("  gone: " + pad + " -- " + left);
        }
        List<SimGamepad.Pad> came = sdl.arrived();
        if (!came.isEmpty()) {
            assign();
            for (SimGamepad.Pad pad : came) {
                out.println("  new gamepad: " + pad);
            }
        }
        census();
    }

    // --- the assignment ---------------------------------------------------

    /**
     * The assignment before any gesture: the only pad, or what the store says.
     *
     * <p>A stored serial number is used only when a pad plugged in reports it.
     * A stale entry asks for the gesture again rather than silently driving the
     * wrong robot, and where two pads report one serial number the higher device
     * id takes it.
     *
     * <p>Called again after a pad arrives, and then it does nothing unless both
     * players are free: a pad arriving beside one that is already driving asks
     * for the gesture rather than reading the store. The gesture is always
     * available, which is what makes the simple rule enough.
     */
    private void assign() {
        if (player1 != null || player2 != null) {
            return;
        }
        List<SimGamepad.Pad> all = sdl.pads();
        List<SimGamepad.Pad> usable = new ArrayList<>();
        for (SimGamepad.Pad pad : all) {
            if (accepted(pad) && !passedOver(pad, all)) {
                usable.add(pad);
            }
        }
        if (usable.size() == 1) {
            player1 = usable.get(0);
            source.put(player1, Source.ONLY_PAD);
            return;
        }
        Map<String, String> stored = read(store);
        player1 = highestWithSerial(all, stored.get(KEY1));
        player2 = highestWithSerial(all, stored.get(KEY2));
        if (player1 != null && player1 == player2) {
            // One serial written to both keys names one pad for both players.
            player1 = null;
            player2 = null;
        }
        if (player1 != null) {
            source.put(player1, Source.STORED);
        }
        if (player2 != null) {
            source.put(player2, Source.STORED);
        }
    }

    /**
     * True when the library reports a serial number for this pad, which is any
     * string of one character or more.
     *
     * <p>A serial number is the only handle that survives unplugging, so a pad
     * without one cannot be remembered and cannot be told from another of the
     * same model. Such a pad is not used: it is not read, no slot takes it and
     * no gesture moves it.
     */
    static boolean accepted(SimGamepad.Pad pad) {
        return pad.serial != null && !pad.serial.isEmpty();
    }

    /**
     * The pad a stored serial number names: the one with the highest device id
     * where several report the same number.
     *
     * <p>Two pads reporting one serial number is not a case this tool is
     * required to work for. The highest id is a rule rather than a refusal so
     * that it does something rather than nothing, and the gesture is still
     * there to say otherwise.
     */
    static SimGamepad.Pad highestWithSerial(List<SimGamepad.Pad> pads, String serial) {
        if (serial == null || serial.isEmpty()) {
            return null;
        }
        SimGamepad.Pad found = null;
        for (SimGamepad.Pad pad : pads) {
            // No accepted() here: serial is not empty, so matching it makes the pad
            // accepted by definition.
            if (serial.equals(pad.serial) && (found == null || pad.id > found.id)) {
                found = pad;
            }
        }
        return found;
    }

    /** True when this pad is the one another with its serial number stands in for. */
    static boolean passedOver(SimGamepad.Pad pad, List<SimGamepad.Pad> pads) {
        return accepted(pad) && highestWithSerial(pads, pad.serial) != pad;
    }

    /**
     * The slot a gamepad's current state claims, or 0 for none.
     *
     * <p>Held, not toggled: the gesture is Start down and A down at the same
     * moment, the way the Driver Station's is, so pressing A on its own does
     * nothing. Whether the slot is free is not asked, because a gesture takes an
     * occupied slot.
     */
    static int claimedPlayer(Gamepad state) {
        if (!state.start) {
            return 0;
        }
        if (state.a) {
            return 1;
        }
        if (state.b) {
            return 2;
        }
        return 0;
    }

    /**
     * The two slots after a gesture: {@code gamepad1} at 0, {@code gamepad2} at
     * 1, either of them null for empty.
     *
     * <p>Pure, and it is the whole slot rule for a gesture. The gamepad making
     * the gesture takes the slot it asked for, whoever was in it. The gamepad
     * displaced moves to the other slot when that one is free, and is left with
     * none when it is not, which takes three gamepads to happen. A gamepad holds
     * one slot, so one claiming the other slot lets go of the one it had, and the
     * two swap.
     */
    static SimGamepad.Pad[] displace(SimGamepad.Pad claimant, int player,
            SimGamepad.Pad player1, SimGamepad.Pad player2) {
        SimGamepad.Pad wanted = player == 1 ? player1 : player2;
        SimGamepad.Pad other = player == 1 ? player2 : player1;
        if (wanted == claimant) {
            return new SimGamepad.Pad[] {player1, player2};
        }
        if (other == claimant) {
            other = null;
        }
        if (other == null) {
            other = wanted;
        }
        return player == 1
                ? new SimGamepad.Pad[] {claimant, other}
                : new SimGamepad.Pad[] {other, claimant};
    }

    private void take(SimGamepad.Pad pad, int player) {
        SimGamepad.Pad[] slots = displace(pad, player, player1, player2);
        if (slots[0] == player1 && slots[1] == player2) {
            return;
        }
        SimGamepad.Pad was1 = player1;
        SimGamepad.Pad was2 = player2;
        player1 = slots[0];
        player2 = slots[1];
        // Only a gamepad whose slot changed had it decided by this gesture.
        if (player1 != null && player1 != was1) {
            source.put(player1, Source.GESTURE);
        }
        if (player2 != null && player2 != was2) {
            source.put(player2, Source.GESTURE);
        }
        out.println("  claimed: " + pad + " -- gamepad" + player);
        store();
        census();
    }

    // --- the store --------------------------------------------------------

    /** The repository's own {@code local.properties}, found by walking up. */
    static Path localProperties() {
        File dir = new File(".").getAbsoluteFile();
        while (dir != null) {
            if (new File(dir, "settings.gradle").isFile()) {
                return dir.toPath().resolve("local.properties");
            }
            dir = dir.getParentFile();
        }
        return new File("local.properties").getAbsoluteFile().toPath();
    }

    /**
     * Every {@code key=value} line, in order, with comments and blanks dropped.
     *
     * <p>Hand-parsed rather than {@code Properties.load}, because writing it
     * back has to leave every other line of somebody's {@code local.properties}
     * alone, {@code sdk.dir} and its header comment included.
     */
    static Map<String, String> read(Path file) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : lines(file)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                continue;
            }
            int equals = trimmed.indexOf('=');
            if (equals > 0) {
                values.put(trimmed.substring(0, equals).trim(),
                        trimmed.substring(equals + 1).trim());
            }
        }
        return values;
    }

    private static List<String> lines(Path file) {
        try {
            return Files.exists(file)
                    ? Files.readAllLines(file, StandardCharsets.UTF_8)
                    : new ArrayList<String>();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
    }

    private void store() {
        write(store, player1 == null ? null : player1.serial,
                player2 == null ? null : player2.serial);
    }

    /**
     * Rewrites only the two {@code sim.gamepad} lines, in place where they exist
     * and appended where they do not.
     */
    static void write(Path file, String serial1, String serial2) {
        List<String> out = new ArrayList<>();
        boolean wrote1 = false;
        boolean wrote2 = false;
        for (String line : lines(file)) {
            String key = line.trim().startsWith("#") ? "" : key(line);
            if (KEY1.equals(key)) {
                if (!wrote1) {
                    add(out, KEY1, serial1);
                    wrote1 = true;
                }
            } else if (KEY2.equals(key)) {
                if (!wrote2) {
                    add(out, KEY2, serial2);
                    wrote2 = true;
                }
            } else {
                out.add(line);
            }
        }
        if (!wrote1) {
            add(out, KEY1, serial1);
        }
        if (!wrote2) {
            add(out, KEY2, serial2);
        }
        try {
            Files.write(file, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write " + file, e);
        }
    }

    private static void add(List<String> out, String key, String serial) {
        if (serial != null && !serial.isEmpty()) {
            out.add(key + "=" + serial);
        }
    }

    private static String key(String line) {
        int equals = line.indexOf('=');
        return equals > 0 ? line.substring(0, equals).trim() : "";
    }

    // --- the display ------------------------------------------------------

    /**
     * One line per gamepad the library is listing, each saying what that gamepad
     * is doing, and nothing at all when it all reads the same as last time.
     *
     * <p>Startup is the first census rather than a shape of its own, so the
     * lines a student sees before a run are the lines a cable coming out
     * produces. The event lines that name what happened are printed by whoever
     * saw it happen; this says where everything stands afterwards.
     */
    private void census() {
        String body = censusText(sdl.pads(), player1, player2, source);
        if (body.equals(lastCensus)) {
            return;
        }
        lastCensus = body;
        out.print(body);
    }

    /**
     * The whole census as text: one line per gamepad, and the line asking for
     * the gesture when some gamepad has no slot.
     *
     * <p>Pure, so that what the census says can be read in a test where two
     * gamepads and a shared serial number are a list rather than a bench. The
     * same gamepads and the same slots give the same text, which is how
     * {@link #census()} knows nothing changed.
     */
    static String censusText(List<SimGamepad.Pad> pads, SimGamepad.Pad player1,
            SimGamepad.Pad player2, Map<SimGamepad.Pad, Source> source) {
        if (pads.isEmpty()) {
            return "No gamepad is plugged in. A gamepad object with no gamepad in its"
                    + " slot reads as untouched.\n";
        }
        StringBuilder text = new StringBuilder();
        boolean settled = true;
        for (SimGamepad.Pad pad : pads) {
            State s = state(pad, pads, player1, player2);
            if (s != State.ACTIVE && s != State.NOT_ACCEPTED) {
                // Only a gamepad with no serial number is one no gesture moves.
                settled = false;
            }
            text.append("  ").append(pad).append(" -- ")
                    .append(explain(pad, s, player1, source)).append('\n');
        }
        if (!settled) {
            text.append("Hold Start and press A to drive as gamepad1,"
                    + " or Start and B for gamepad2.\n");
        }
        return text.toString();
    }

    /** What one gamepad's census line says after the two dashes. */
    private static String explain(SimGamepad.Pad pad, State s, SimGamepad.Pad player1,
            Map<SimGamepad.Pad, Source> source) {
        switch (s) {
            case ACTIVE:
                return (pad == player1 ? "gamepad1, " : "gamepad2, ") + because(source.get(pad));
            case NOT_ACCEPTED:
                return "no serial number, so it is not used";
            case PASSED_OVER:
                return "passed over; another gamepad reports this serial number"
                        + " with a higher device id";
            case UNCLAIMED_FULL:
                return "not claimed; both slots are taken, and a gesture takes one anyway";
            default:
                return "not claimed yet";
        }
    }

    /** Why the gamepad in a slot is the one in it. */
    static String because(Source from) {
        if (from == Source.ONLY_PAD) {
            return "the only gamepad plugged in";
        }
        if (from == Source.STORED) {
            return "remembered in local.properties by serial number";
        }
        return "claimed this run";
    }

    // --- the off-rest report ----------------------------------------------

    /** How far a stick or trigger moves before the report names it. */
    static final float OFF_REST = 0.05f;

    /**
     * Every control of one gamepad that is away from its untouched value, as
     * {@code name=value}.
     *
     * <p>Pure, and the threshold is what needs saying: a stick that has been
     * pushed and let go can sit a count or two off centre, about 0.00003 of full
     * scale, so without it a run would name that stick for ever. A button is
     * away when it is pressed.
     *
     * <p>The names come from the sets {@link SimArgs} holds and the values off
     * {@code Gamepad}'s own fields, so this and the control options cannot come
     * to disagree about what a control is called.
     */
    static List<String> offRest(Gamepad state) {
        List<String> touched = new ArrayList<>();
        for (String name : SimArgs.AXES) {
            float value = axis(state, name);
            if (Math.abs(value) > OFF_REST) {
                touched.add(name + "=" + String.format(Locale.US, "%.2f", value));
            }
        }
        for (String name : SimArgs.BUTTONS) {
            if (button(state, name)) {
                touched.add(name + "=true");
            }
        }
        return touched;
    }

    private static float axis(Gamepad state, String name) {
        try {
            return Gamepad.class.getField(name).getFloat(state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no gamepad field called \"" + name + "\"", e);
        }
    }

    private static boolean button(Gamepad state, String name) {
        try {
            return Gamepad.class.getField(name).getBoolean(state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no gamepad field called \"" + name + "\"", e);
        }
    }

    /**
     * What every gamepad being read is having done to it, and one line when
     * everything has been let go.
     *
     * <p>Every twenty-fifth instant, and only when it reads differently from
     * last time, so a stick held still says so once rather than four times a
     * second. The reading is the one {@link #read} already took this instant for
     * a gamepad in a slot, and a fresh one for a gamepad that is only being
     * watched for the gesture.
     */
    public void report() {
        List<SimGamepad.Pad> all = sdl.pads();
        StringBuilder text = new StringBuilder();
        for (SimGamepad.Pad pad : all) {
            State s = state(pad, all, player1, player2);
            if (s == State.NOT_ACCEPTED) {
                continue;
            }
            List<String> touched = offRest(reading(pad, s));
            if (touched.isEmpty()) {
                continue;
            }
            String slot = s != State.ACTIVE ? "no slot"
                    : pad == player1 ? "gamepad1" : "gamepad2";
            text.append("  ").append(pad).append(" -- ").append(slot).append(':');
            for (String control : touched) {
                text.append(' ').append(control);
            }
            text.append('\n');
        }
        String body = text.toString();
        if (body.equals(lastReport)) {
            return;
        }
        lastReport = body;
        out.print(body.isEmpty() ? "  Nothing is touched.\n" : body);
    }

    private Gamepad reading(SimGamepad.Pad pad, State s) {
        if (s == State.ACTIVE) {
            return pad == player1 ? staging1 : staging2;
        }
        sdl.read(pad, probe);
        return probe;
    }
}
