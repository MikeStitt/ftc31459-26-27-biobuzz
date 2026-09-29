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

    /** Where an assignment came from, for the line printed at startup. */
    enum Source {
        /** The only pad plugged in, so no gesture was asked for. */
        ONLY_PAD,
        /** Read back from {@code local.properties} by serial number. */
        STORED,
        /** Claimed with Start and A, or Start and B, during this run. */
        GESTURE,
        /** Not assigned to either player yet. */
        UNCLAIMED,
        /** Another pad reports the same serial number with a higher id. */
        PASSED_OVER,
        /** No serial number, so it is not used at all. */
        NOT_ACCEPTED
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

    SimPads(SimGamepad sdl, Path store, PrintStream out) {
        this.sdl = sdl;
        this.store = store;
        this.out = out;
        assign();
        describe();
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
     * Watches every pad without a slot for Start and A, or Start and B.
     *
     * <p>Every fifth instant. Faster buys nothing: the gesture is held rather
     * than a press, so 50 ms cannot miss one.
     */
    public void gestures() {
        if (player1 == null || player2 == null) {
            claim();
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

    /**
     * True once every pad a gesture could move has a player.
     *
     * <p>True of no pads as well, so it says nothing about whether anything is
     * plugged in; what it decides is whether to ask for the gesture. A pad with
     * no serial number is not counted, because no gesture moves it.
     */
    private boolean settled() {
        for (Source s : source.values()) {
            if (s == Source.UNCLAIMED || s == Source.PASSED_OVER) {
                return false;
            }
        }
        return true;
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
     * player's {@code Gamepad} on the next instant. Both events print a line, in
     * the shape the startup lines use.
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
        if (came.isEmpty()) {
            return;
        }
        assign();
        for (SimGamepad.Pad pad : came) {
            out.println("  new pad: " + pad + " -- " + explain(pad));
        }
        if (!settled()) {
            out.println("Hold Start and press A to drive as gamepad1,"
                    + " or Start and B for gamepad2.");
        }
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
        List<SimGamepad.Pad> all = sdl.pads();
        List<SimGamepad.Pad> usable = new ArrayList<>();
        for (SimGamepad.Pad pad : all) {
            if (pad == player1 || pad == player2) {
                // A pad keeps its slot until it is unplugged or a gesture moves it,
                // so a twin arriving with a higher id does not pass it over.
                usable.add(pad);
                continue;
            }
            if (!accepted(pad)) {
                source.put(pad, Source.NOT_ACCEPTED);
                continue;
            }
            if (passedOver(pad, all)) {
                source.put(pad, Source.PASSED_OVER);
                continue;
            }
            usable.add(pad);
            if (!source.containsKey(pad) || source.get(pad) == Source.PASSED_OVER) {
                source.put(pad, Source.UNCLAIMED);
            }
        }
        if (player1 != null || player2 != null) {
            return;
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

    /** Start and A for player one, Start and B for player two. */
    private void claim() {
        for (SimGamepad.Pad pad : sdl.pads()) {
            Source from = source.get(pad);
            if (from != Source.UNCLAIMED && from != Source.PASSED_OVER) {
                continue;
            }
            sdl.read(pad, probe);
            int player = claimedPlayer(probe, player1 == null, player2 == null);
            if (player != 0) {
                take(pad, player);
            }
        }
    }

    /**
     * The player one pad's current state claims, or 0 for none.
     *
     * <p>Held, not toggled: the gesture is Start down and A down at the same
     * moment, the way the Driver Station's is, so a pad cannot claim a player
     * that already has one and pressing A on its own does nothing.
     */
    static int claimedPlayer(Gamepad state, boolean player1Free, boolean player2Free) {
        if (!state.start) {
            return 0;
        }
        if (state.a && player1Free) {
            return 1;
        }
        if (state.b && player2Free) {
            return 2;
        }
        return 0;
    }

    private void take(SimGamepad.Pad pad, int player) {
        if (player == 1) {
            player1 = pad;
        } else {
            player2 = pad;
        }
        source.put(pad, Source.GESTURE);
        out.println("gamepad" + player + " is now " + pad + ".");
        store();
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

    /** One line per pad, naming it and where its assignment came from. */
    private void describe() {
        if (sdl.pads().isEmpty()) {
            out.println("No gamepad is plugged in. A gamepad object with no gamepad in its"
                    + " slot reads as untouched.");
            return;
        }
        for (SimGamepad.Pad pad : sdl.pads()) {
            out.println("  " + pad + " -- " + explain(pad));
        }
        if (!settled()) {
            out.println("Hold Start and press A to drive as gamepad1,"
                    + " or Start and B for gamepad2.");
        }
    }

    private String explain(SimGamepad.Pad pad) {
        Source from = source.get(pad);
        String player = pad == player1 ? "gamepad1" : pad == player2 ? "gamepad2" : null;
        switch (from) {
            case ONLY_PAD:
                return player + ", the only pad plugged in";
            case STORED:
                return player + ", remembered in local.properties by serial";
            case GESTURE:
                return player + ", claimed this run";
            case NOT_ACCEPTED:
                return "no serial number, so it is not used";
            case PASSED_OVER:
                return "passed over; another pad reports this serial number with a higher id";
            default:
                return "not claimed yet";
        }
    }
}
