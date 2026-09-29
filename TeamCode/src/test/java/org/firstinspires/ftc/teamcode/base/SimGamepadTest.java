package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * The arithmetic {@link SimGamepad} turns SDL's readings into FTC's with, and
 * which pads arrived or went away. No pad is plugged in for this, and SDL is not
 * started: opening a pad is watched by hand, because a stick has to be pushed.
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

    /** A pad with no SDL behind it: a handle of 0 reads nothing. */
    private static SimGamepad.Pad pad(int id) {
        return new SimGamepad.Pad(id, 0L, "pad " + id, "serial" + id);
    }

    @Test
    public void anIdNotHeldYetHasArrived() {
        List<SimGamepad.Pad> held = new ArrayList<>(Arrays.asList(pad(1)));
        assertEquals(Arrays.asList(4), SimGamepad.addedIds(new int[] {1, 4}, held));
    }

    @Test
    public void anIdAlreadyHeldHasNotArrived() {
        List<SimGamepad.Pad> held = new ArrayList<>(Arrays.asList(pad(1), pad(4)));
        assertTrue(SimGamepad.addedIds(new int[] {1, 4}, held).isEmpty());
    }

    @Test
    public void everyIdArrivesWhenNothingIsHeld() {
        assertEquals(Arrays.asList(1, 4),
                SimGamepad.addedIds(new int[] {1, 4}, new ArrayList<SimGamepad.Pad>()));
    }

    @Test
    public void aPadNoLongerListedHasGone() {
        SimGamepad.Pad one = pad(1);
        SimGamepad.Pad four = pad(4);
        List<SimGamepad.Pad> held = new ArrayList<>(Arrays.asList(one, four));
        assertEquals(Arrays.asList(four), SimGamepad.goneFrom(new int[] {1}, held));
    }

    @Test
    public void nothingHasGoneWhileEveryIdIsStillListed() {
        List<SimGamepad.Pad> held = new ArrayList<>(Arrays.asList(pad(1), pad(4)));
        assertTrue(SimGamepad.goneFrom(new int[] {4, 1}, held).isEmpty());
    }

    @Test
    public void everyPadHasGoneWhenNothingIsListed() {
        List<SimGamepad.Pad> held = new ArrayList<>(Arrays.asList(pad(1), pad(4)));
        assertEquals(2, SimGamepad.goneFrom(new int[] {}, held).size());
    }
}
