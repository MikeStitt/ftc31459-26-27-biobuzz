package org.firstinspires.ftc.teamcode.base;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLEvents.SDL_PumpEvents;
import static org.lwjgl.sdl.SDLGamepad.SDL_CloseGamepad;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_BACK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_EAST;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_START;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_WEST;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadAxis;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadButton;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadName;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadSerial;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepads;
import static org.lwjgl.sdl.SDLGamepad.SDL_OpenGamepad;
import static org.lwjgl.sdl.SDLGamepad.SDL_UpdateGamepads;
import static org.lwjgl.sdl.SDLHints.SDL_HINT_JOYSTICK_MFI;
import static org.lwjgl.sdl.SDLHints.SDL_SetHint;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_EVENTS;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_GAMEPAD;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_JOYSTICK;
import static org.lwjgl.sdl.SDLInit.SDL_Init;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLStdinc.SDL_free;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The gamepads plugged into the laptop, as {@link Gamepad} fields.
 *
 * <p>This class owns SDL and nothing else: opening it, closing it, and turning
 * one pad's readings into one {@code Gamepad}. Which pad is {@code gamepad1} is
 * somebody else's question.
 *
 * <p>SDL3 is the same library WPILib's own simulator reads a gamepad with, and
 * on macOS it offers two backends that disagree about a pad. Apple's
 * GameController framework inverts the left stick on the pad this was written
 * against, so {@code SDL_JOYSTICK_MFI} is turned off and SDL reads IOKit
 * instead. Measured, both ways: {@code sim.sticks.input} in
 * {@code .docs/open-work.md}.
 *
 * <p>Passes when: SimGamepadTest, which runs with no pad plugged in.
 */
public final class SimGamepad implements AutoCloseable {

    /** How long {@link #open} waits for the first pad to appear. */
    private static final long DISCOVERY_MS = 2000;

    private static final long DISCOVERY_STEP_MS = 100;

    /** Full scale one way. A stick reads -32768 to 32767, so the two differ. */
    private static final float FULL_NEGATIVE = 32768f;

    private static final float FULL_POSITIVE = 32767f;

    /** One pad SDL has open, and what it calls itself. */
    public static final class Pad {

        /** SDL's id for this pad, which changes between runs. */
        public final int id;

        /** What the pad calls itself, for a line a reader can recognise. */
        public final String name;

        /**
         * The pad's own serial, or null where the pad or the backend has none.
         * This is the only handle that survives unplugging and plugging in
         * again, so it is what an assignment is stored against.
         */
        public final String serial;

        private final long handle;

        /**
         * Package-private, not private, so a test can make a pad with no SDL
         * behind it. A handle of 0 reads nothing, which is all
         * {@code SimPadsTest} needs.
         */
        Pad(int id, long handle, String name, String serial) {
            this.id = id;
            this.handle = handle;
            this.name = name;
            this.serial = serial;
        }

        @Override
        public String toString() {
            return "id " + id + " \"" + name + "\""
                    + (serial == null ? ", no serial" : ", serial " + serial);
        }
    }

    private final List<Pad> pads;

    private boolean closed;

    private SimGamepad(List<Pad> pads) {
        this.pads = pads;
    }

    /**
     * Starts SDL and opens every pad it can see, waiting up to
     * {@value #DISCOVERY_MS} ms for the first one.
     *
     * <p>The wait is not politeness. Measured on 2026-09-28:
     * {@code SDL_GetGamepads()} saw none on the first pass and one about 100 ms
     * later, so enumerating once finds nothing.
     *
     * @throws IllegalStateException if SDL will not start at all
     */
    public static SimGamepad open() {
        SDL_SetHint(SDL_HINT_JOYSTICK_MFI, "0");
        if (!SDL_Init(SDL_INIT_GAMEPAD | SDL_INIT_JOYSTICK | SDL_INIT_EVENTS)) {
            throw new IllegalStateException("SDL would not start: " + SDL_GetError());
        }
        List<Pad> pads = new ArrayList<>();
        try {
            int[] ids = new int[0];
            for (long waited = 0; waited <= DISCOVERY_MS; waited += DISCOVERY_STEP_MS) {
                SDL_PumpEvents();
                SDL_UpdateGamepads();
                ids = list();
                if (ids.length > 0) {
                    break;
                }
                OpModeHarness.sleep(DISCOVERY_STEP_MS);
            }
            for (int id : ids) {
                long handle = SDL_OpenGamepad(id);
                if (handle == 0L) {
                    continue;
                }
                pads.add(new Pad(id, handle, SDL_GetGamepadName(handle),
                        SDL_GetGamepadSerial(handle)));
            }
        } catch (RuntimeException e) {
            for (Pad pad : pads) {
                SDL_CloseGamepad(pad.handle);
            }
            SDL_Quit();
            throw e;
        }
        return new SimGamepad(pads);
    }

    /**
     * The ids SDL is listing, copied out of SDL's own memory.
     *
     * <p>{@code SDL_GetGamepads} mallocs the array it returns and the caller
     * owns it. Measured on 2026-09-28: 8 million calls without the free grew the
     * process by 26 MB/s and with it by nothing, and two calls in a row return
     * different addresses rather than one cached block.
     */
    private static int[] list() {
        IntBuffer ids = SDL_GetGamepads();
        if (ids == null) {
            return new int[0];
        }
        try {
            int[] out = new int[ids.remaining()];
            for (int i = 0; i < out.length; i++) {
                out[i] = ids.get(ids.position() + i);
            }
            return out;
        } finally {
            SDL_free(ids);
        }
    }

    /**
     * Pads SDL is listing that were not held, now opened and added to
     * {@link #pads()}.
     *
     * <p>How often this is called is {@link SimPads}'s business, which owns the
     * policy. A rescan costs about 1 us against the simulator's 5 ms step, and
     * {@link #departed()} lists again rather than sharing the result, because
     * 1 us twice is not worth a shape that has to hand an array around.
     */
    public List<Pad> arrived() {
        List<Pad> added = new ArrayList<>();
        for (int id : addedIds(list(), pads)) {
            long handle = SDL_OpenGamepad(id);
            if (handle == 0L) {
                continue;
            }
            Pad pad = new Pad(id, handle, SDL_GetGamepadName(handle),
                    SDL_GetGamepadSerial(handle));
            pads.add(pad);
            added.add(pad);
        }
        return added;
    }

    /**
     * Pads SDL has stopped listing, now removed from {@link #pads()} and closed.
     *
     * <p>A pad SDL stops listing is a pad that was unplugged, and that is the
     * whole test: what the driver reports for a disconnected pad is never asked.
     * An id SDL reuses for a different pad inside one run would be taken for the
     * pad that left, which is untested and has not been seen.
     */
    public List<Pad> departed() {
        List<Pad> gone = goneFrom(list(), pads);
        for (Pad pad : gone) {
            pads.remove(pad);
            SDL_CloseGamepad(pad.handle);
        }
        return gone;
    }

    /** Ids in {@code listed} that no pad in {@code held} has. */
    static List<Integer> addedIds(int[] listed, List<Pad> held) {
        List<Integer> out = new ArrayList<>();
        for (int id : listed) {
            if (withId(held, id) == null) {
                out.add(id);
            }
        }
        return out;
    }

    /** Pads in {@code held} whose id is not in {@code listed}. */
    static List<Pad> goneFrom(int[] listed, List<Pad> held) {
        List<Pad> out = new ArrayList<>();
        for (Pad pad : held) {
            boolean still = false;
            for (int id : listed) {
                if (pad.id == id) {
                    still = true;
                    break;
                }
            }
            if (!still) {
                out.add(pad);
            }
        }
        return out;
    }

    private static Pad withId(List<Pad> pads, int id) {
        for (Pad pad : pads) {
            if (pad.id == id) {
                return pad;
            }
        }
        return null;
    }

    /** The pads SDL has open, in the order it listed them. Possibly none. */
    public List<Pad> pads() {
        return Collections.unmodifiableList(pads);
    }

    /**
     * Reads every pad's current state from the driver.
     *
     * <p>Once a loop is enough: {@link #read} only copies what this call
     * fetched.
     *
     * <p>It pumps the event queue as well, the way {@link #open}'s discovery
     * loop does, so that {@link #arrived()} is listing against the same state
     * discovery lists against. Whether {@code SDL_UpdateGamepads} alone would
     * notice a pad plugged in is unmeasured; pumping costs 0.6 us, measured, so
     * the question was not worth leaving open.
     */
    public void update() {
        SDL_PumpEvents();
        SDL_UpdateGamepads();
    }

    /**
     * Writes one pad's sticks, triggers and buttons into {@code into}.
     *
     * <p>Every value is a pass-through. FTC's y is negative forward and its x
     * positive to the right, and SDL's readings carry the same two signs, so
     * nothing here flips one. The scaling is WPILib's own: a negative reading
     * over 32768 and a positive one over 32767, so a stick at its stop is
     * exactly 1.0 either way, and a trigger is its reading over 32767 for FTC's
     * 0.0 to 1.0.
     *
     * <p>This writes the 21 controls a pad has and nothing else. The nine
     * fields the SDK derives from them, the PS4 aliases and both
     * {@code _trigger_pressed} among them, and the 50 {@code WasPressed} and
     * {@code WasReleased} methods, come from handing this reading to
     * {@code Gamepad.copy}, which is {@link SimPads}'s job. So {@code into} is
     * a staging pad rather than the one an OpMode reads.
     */
    public void read(Pad pad, Gamepad into) {
        into.left_stick_x = stick(pad, SDL_GAMEPAD_AXIS_LEFTX);
        into.left_stick_y = stick(pad, SDL_GAMEPAD_AXIS_LEFTY);
        into.right_stick_x = stick(pad, SDL_GAMEPAD_AXIS_RIGHTX);
        into.right_stick_y = stick(pad, SDL_GAMEPAD_AXIS_RIGHTY);
        into.left_trigger = trigger(pad, SDL_GAMEPAD_AXIS_LEFT_TRIGGER);
        into.right_trigger = trigger(pad, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER);

        into.a = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_SOUTH);
        into.b = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_EAST);
        into.x = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_WEST);
        into.y = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_NORTH);
        into.back = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_BACK);
        into.guide = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_GUIDE);
        into.start = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_START);
        into.left_stick_button =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_LEFT_STICK);
        into.right_stick_button =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_RIGHT_STICK);
        into.left_bumper =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
        into.right_bumper =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
        into.dpad_up = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_UP);
        into.dpad_down = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_DOWN);
        into.dpad_left = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_LEFT);
        into.dpad_right = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_RIGHT);
    }

    private static float stick(Pad pad, int axis) {
        return stickScale(SDL_GetGamepadAxis(pad.handle, axis));
    }

    private static float trigger(Pad pad, int axis) {
        return triggerScale(SDL_GetGamepadAxis(pad.handle, axis));
    }

    /**
     * One stick reading as FTC's -1.0 to 1.0.
     *
     * <p>The two ends divide by different numbers because the range is not
     * symmetric: dividing everything by 32767 would make a stick at its stop
     * read 1.00003 one way.
     */
    static float stickScale(short raw) {
        return raw < 0 ? raw / FULL_NEGATIVE : raw / FULL_POSITIVE;
    }

    /** One trigger reading as FTC's 0.0 to 1.0. A resting trigger reads 0. */
    static float triggerScale(short raw) {
        return raw <= 0 ? 0f : raw / FULL_POSITIVE;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (Pad pad : pads) {
            SDL_CloseGamepad(pad.handle);
        }
        SDL_Quit();
    }
}
