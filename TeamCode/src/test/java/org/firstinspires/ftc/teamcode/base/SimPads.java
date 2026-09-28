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

    /**
     * How often the pads plugged in are looked at again.
     *
     * <p>A rescan costs about 1 us against the simulator's 5 ms step, so this is
     * not about the mean. It is about the outlier: 2000 rescans had one at
     * 842 us, and a quarter of a second is far faster than a hand with a cable.
     */
    private static final long RESCAN_MS = 250;

    static final String KEY1 = "sim.gamepad1.serial";

    static final String KEY2 = "sim.gamepad2.serial";

    /** Where an assignment came from, for the line printed at startup. */
    enum Source {
        /** The only pad plugged in, so no gesture was asked for. */
        ONLY_PAD,
        /** Read back from {@code local.properties} by serial. */
        STORED,
        /** Claimed with Start and A, or Start and B, during this run. */
        GESTURE,
        /** Not assigned to either player yet. */
        UNCLAIMED
    }

    private final SimGamepad sdl;

    private final Path store;

    private final PrintStream out;

    /** Scratch, so a pad can be read for its gesture without reaching a lesson. */
    private final Gamepad probe = new Gamepad();

    private final Map<SimGamepad.Pad, Source> source = new LinkedHashMap<>();

    private SimGamepad.Pad player1;

    private SimGamepad.Pad player2;

    private long nextScan;

    SimPads(SimGamepad sdl, Path store, PrintStream out) {
        this.sdl = sdl;
        this.store = store;
        this.out = out;
        assign();
        describe();
        nextScan = System.currentTimeMillis() + RESCAN_MS;
    }

    /** Opens SDL, loads any stored assignment, and says what it found. */
    public static SimPads open() {
        return new SimPads(SimGamepad.open(), localProperties(), System.out);
    }

    /**
     * Reads the pads, hands each player's readings to its {@code Gamepad}, and
     * watches an unassigned pad for its claiming gesture.
     *
     * <p>Called once a loop. A {@code Gamepad} with no pad behind it is left
     * exactly as it was, which is resting.
     */
    public void update(Gamepad gamepad1, Gamepad gamepad2) {
        sdl.update();
        rescan(gamepad1, gamepad2);
        if (player1 == null || player2 == null) {
            claim();
        }
        if (player1 != null) {
            sdl.read(player1, gamepad1);
        }
        if (player2 != null) {
            sdl.read(player2, gamepad2);
        }
    }

    /** True while nothing is plugged in at all, so there is nothing to assign. */
    public boolean empty() {
        return sdl.pads().isEmpty();
    }

    /**
     * True once every pad plugged in has a player.
     *
     * <p>True of no pads, which is why {@link #main} asks {@link #empty()} as
     * well: zero pads all having a player is not a reason to stop waiting.
     */
    public boolean settled() {
        for (Source s : source.values()) {
            if (s == Source.UNCLAIMED) {
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
     * unplugged, at most every {@value #RESCAN_MS} ms.
     *
     * <p>A pad that goes away releases its player and that player's
     * {@code Gamepad} is written back to rest, so a robot being driven forward
     * stops rather than holding the last reading. Both events print a line, in
     * the shape the startup lines use.
     */
    private void rescan(Gamepad gamepad1, Gamepad gamepad2) {
        long now = System.currentTimeMillis();
        if (now < nextScan) {
            return;
        }
        nextScan = now + RESCAN_MS;
        for (SimGamepad.Pad pad : sdl.departed()) {
            source.remove(pad);
            String left = "was not claimed";
            if (pad == player1) {
                player1 = null;
                SimGamepad.rest(gamepad1);
                left = "gamepad1 is back to rest";
            } else if (pad == player2) {
                player2 = null;
                SimGamepad.rest(gamepad2);
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
     * <p>A stored serial is used only when it still names exactly one pad that
     * is plugged in. Anything else -- a pad missing, two pads answering to the
     * same serial, no serial at all -- asks for the gesture again, because a
     * stale entry silently driving the wrong robot is worse than a prompt.
     *
     * <p>Called again after a pad arrives, and then it does nothing unless both
     * players are free: a pad arriving beside one that is already driving asks
     * for the gesture rather than reading the store. The gesture is always
     * available, which is what makes the simple rule enough.
     */
    private void assign() {
        List<SimGamepad.Pad> pads = sdl.pads();
        for (SimGamepad.Pad pad : pads) {
            if (!source.containsKey(pad)) {
                source.put(pad, Source.UNCLAIMED);
            }
        }
        if (player1 != null || player2 != null) {
            return;
        }
        if (pads.size() == 1) {
            player1 = pads.get(0);
            source.put(player1, Source.ONLY_PAD);
            return;
        }
        Map<String, String> stored = read(store);
        player1 = onlyPadWithSerial(pads, stored.get(KEY1));
        player2 = onlyPadWithSerial(pads, stored.get(KEY2));
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

    static SimGamepad.Pad onlyPadWithSerial(List<SimGamepad.Pad> pads, String serial) {
        if (serial == null || serial.isEmpty()) {
            return null;
        }
        SimGamepad.Pad found = null;
        for (SimGamepad.Pad pad : pads) {
            if (serial.equals(pad.serial)) {
                if (found != null) {
                    return null;
                }
                found = pad;
            }
        }
        return found;
    }

    /** Start and A for player one, Start and B for player two. */
    private void claim() {
        for (SimGamepad.Pad pad : sdl.pads()) {
            if (source.get(pad) != Source.UNCLAIMED) {
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
            out.println("No gamepad found. The lesson runs with the sticks at rest;"
                    + " simRun's own arguments still set them.");
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
            default:
                return "not claimed yet";
        }
    }

    /**
     * Does the assignment on its own, so it is done before a lesson rather than
     * during one. {@code ./gradlew :TeamCode:simPads}.
     */
    public static void main(String[] args) {
        long seconds = args.length > 0 ? Long.parseLong(args[0]) : 60;
        try (SimPads pads = SimPads.open()) {
            long end = System.currentTimeMillis() + seconds * 1000;
            Gamepad ignored1 = new Gamepad();
            Gamepad ignored2 = new Gamepad();
            while ((pads.empty() || !pads.settled()) && System.currentTimeMillis() < end) {
                pads.update(ignored1, ignored2);
                OpModeHarness.sleep(20);
            }
            System.out.println(verdict(pads.empty(), pads.settled(), seconds));
        }
    }

    /**
     * The line that ends a {@code simPads} run.
     *
     * <p>Three cases, not two. Before a pad could arrive mid-run there was no
     * point waiting for one, and no pad at all ended the run saying every pad
     * had a player, which was true and useless.
     */
    static String verdict(boolean empty, boolean settled, long seconds) {
        if (empty) {
            return "No pad was plugged in after " + seconds + " s.";
        }
        if (settled) {
            return "Every pad has a player; run a lesson.";
        }
        return "Gave up after " + seconds + " s with a pad unclaimed.";
    }
}
