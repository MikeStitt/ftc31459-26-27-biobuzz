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

        private Pad(int id, long handle, String name, String serial) {
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
            IntBuffer ids = null;
            for (long waited = 0; waited <= DISCOVERY_MS; waited += DISCOVERY_STEP_MS) {
                SDL_PumpEvents();
                SDL_UpdateGamepads();
                ids = SDL_GetGamepads();
                if (ids != null && ids.remaining() > 0) {
                    break;
                }
                OpModeHarness.sleep(DISCOVERY_STEP_MS);
            }
            int count = ids == null ? 0 : ids.remaining();
            for (int i = 0; i < count; i++) {
                int id = ids.get(i);
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

    /** The pads SDL has open, in the order it listed them. Possibly none. */
    public List<Pad> pads() {
        return Collections.unmodifiableList(pads);
    }

    /**
     * Reads every pad's current state from the driver.
     *
     * <p>Once a loop is enough: {@link #read} only copies what this call
     * fetched.
     */
    public void update() {
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
     * <p>FTC's PS4 aliases and its two {@code _trigger_pressed} fields are left
     * alone, because no lesson reads one.
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
