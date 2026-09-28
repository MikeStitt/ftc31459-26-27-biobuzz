package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The arithmetic {@link SimGamepad} turns SDL's readings into FTC's with. No pad
 * is plugged in for this, and SDL is not started: opening a pad is watched by
 * hand, because a stick has to be pushed.
 */
public final class SimGamepadTest {

    @Test
    public void aStickAtEitherStopIsExactlyOne() {
        assertEquals("pushed one way", -1.0f, SimGamepad.stickScale((short) -32768), 0f);
        assertEquals("pushed the other", 1.0f, SimGamepad.stickScale((short) 32767), 0f);
    }

    @Test
    public void aRestingStickIsExactlyZero() {
        assertEquals(0.0f, SimGamepad.stickScale((short) 0), 0f);
    }

    @Test
    public void halfScaleReadsAboutAHalf() {
        assertEquals("negative half", -0.5f, SimGamepad.stickScale((short) -16384), 0.001f);
        assertEquals("positive half", 0.5f, SimGamepad.stickScale((short) 16384), 0.001f);
    }

    @Test
    public void aSqueezedTriggerIsOneAndArestingOneIsZero() {
        assertEquals("squeezed", 1.0f, SimGamepad.triggerScale((short) 32767), 0f);
        assertEquals("resting", 0.0f, SimGamepad.triggerScale((short) 0), 0f);
    }

    @Test
    public void aTriggerNeverGoesNegative() {
        assertEquals(0.0f, SimGamepad.triggerScale((short) -1), 0f);
    }
}
