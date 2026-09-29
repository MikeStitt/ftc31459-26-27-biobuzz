package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The parts of {@link SimPads} that can go wrong without a pad plugged in:
 * whether an empty slot reads untouched, whether a stored serial is trusted, and
 * whether writing the assignment leaves the rest of somebody's
 * {@code local.properties} alone.
 *
 * <p>The gesture itself needs a hand on a pad and is not here.
 */
public final class SimPadsTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static SimGamepad.Pad pad(int id, String serial) {
        return new SimGamepad.Pad(id, 0L, "pad " + id, serial);
    }

    // --- an empty slot reads untouched ------------------------------------

    /**
     * {@code reset()} is what an empty slot gets, in place of a list of fields
     * written out by hand. This compares every field the SDK declares, so a
     * field added to {@code Gamepad} later cannot slip past by being missing
     * from a list nobody updated.
     */
    @Test
    public void anEmptySlotReadsLikeAFreshGamepad() throws Exception {
        Gamepad held = new Gamepad();
        held.left_stick_y = -1f;
        held.right_stick_x = 0.5f;
        held.left_trigger = 1f;
        held.right_trigger = 1f;
        held.a = true;
        held.start = true;
        held.dpad_left = true;
        held.left_bumper = true;
        held.right_stick_button = true;

        held.reset();

        Gamepad fresh = new Gamepad();
        List<String> differing = new ArrayList<>();
        for (Field f : Gamepad.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!String.valueOf(f.get(held)).equals(String.valueOf(f.get(fresh)))) {
                differing.add(f.getName());
            }
        }
        assertEquals("fields a fresh pad and a reset one disagree on",
                Collections.emptyList(), differing);
    }

    // --- which pads are accepted ------------------------------------------

    @Test
    public void aPadIsAcceptedOnceItReportsASerialNumberOfAnyLength() {
        assertTrue("one character is a serial number", SimPads.accepted(pad(1, "X")));
        assertTrue(SimPads.accepted(pad(1, "1DD5F3D")));
        assertFalse("no serial number at all", SimPads.accepted(pad(1, null)));
        assertFalse("an empty string is not a serial number", SimPads.accepted(pad(1, "")));
    }

    // --- whether a stored serial number is trusted ------------------------

    @Test
    public void oneSerialNumberNamingOnePadIsTheAssignment() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        assertSame(pads.get(1), SimPads.highestWithSerial(pads, "BBB"));
    }

    @Test
    public void aSerialNumberNoPadAnswersToIsNotTrusted() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertNull("the pad it named is unplugged", SimPads.highestWithSerial(pads, "BBB"));
    }

    @Test
    public void aSerialNumberTwoPadsAnswerToTakesTheHigherDeviceId() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(7, "SAME"), pad(3, "SAME"));
        assertSame("the highest id, whichever order they were listed in",
                pads.get(0), SimPads.highestWithSerial(pads, "SAME"));
        List<SimGamepad.Pad> other = Arrays.asList(pad(3, "SAME"), pad(7, "SAME"));
        assertSame(other.get(1), SimPads.highestWithSerial(other, "SAME"));
    }

    @Test
    public void aStoredValueThatIsNotASerialNumberNamesNoPad() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertNull("nothing stored", SimPads.highestWithSerial(pads, null));
        assertNull("an empty entry", SimPads.highestWithSerial(pads, ""));
    }

    @Test
    public void aPadWithNoSerialNumberFillsNoSlot() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, null), pad(2, ""));
        assertNull("a pad with no serial number", SimPads.highestWithSerial(pads, "AAA"));
        assertNull("and an empty stored entry names neither of them",
                SimPads.highestWithSerial(pads, ""));
    }

    // --- which pad a shared serial number passes over ---------------------

    @Test
    public void theLowerDeviceIdIsThePadPassedOver() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, "SAME"), pad(7, "SAME"));
        assertTrue("id 3 against id 7", SimPads.passedOver(pads.get(0), pads));
        assertFalse("id 7 is the one the serial number names",
                SimPads.passedOver(pads.get(1), pads));
    }

    @Test
    public void twoPadsWithDifferentSerialNumbersPassOverNeither() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, "AAA"), pad(7, "BBB"));
        assertFalse(SimPads.passedOver(pads.get(0), pads));
        assertFalse(SimPads.passedOver(pads.get(1), pads));
    }

    @Test
    public void aPadWithNoSerialNumberIsNotPassedOverButIsNotUsedEither() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, null), pad(7, null));
        assertFalse("passed over is about a serial number two pads share",
                SimPads.passedOver(pads.get(0), pads));
        assertFalse(SimPads.accepted(pads.get(0)));
        assertFalse(SimPads.accepted(pads.get(1)));
    }

    // --- which of the five states a gamepad is in -------------------------

    @Test
    public void aGamepadInASlotIsActive() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        assertEquals(SimPads.State.ACTIVE,
                SimPads.state(pads.get(0), pads, pads.get(0), null));
        assertEquals(SimPads.State.ACTIVE,
                SimPads.state(pads.get(1), pads, null, pads.get(1)));
    }

    @Test
    public void aGamepadWithNoSerialNumberIsNotAccepted() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, null), pad(2, ""));
        assertEquals(SimPads.State.NOT_ACCEPTED,
                SimPads.state(pads.get(0), pads, null, null));
        assertEquals("an empty string is no serial number either",
                SimPads.State.NOT_ACCEPTED, SimPads.state(pads.get(1), pads, null, null));
    }

    @Test
    public void theLowerDeviceIdOfTwoAnsweringToOneSerialNumberIsPassedOver() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, "SAME"), pad(7, "SAME"));
        assertEquals(SimPads.State.PASSED_OVER,
                SimPads.state(pads.get(0), pads, null, null));
        assertEquals(SimPads.State.UNCLAIMED_FREE,
                SimPads.state(pads.get(1), pads, null, null));
    }

    @Test
    public void aGamepadHoldingASlotIsNotPassedOverByATwinWithAHigherId() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, "SAME"), pad(7, "SAME"));
        assertEquals("a slot is kept until the cable comes out or a gesture moves it",
                SimPads.State.ACTIVE, SimPads.state(pads.get(0), pads, pads.get(0), null));
    }

    @Test
    public void anUnclaimedGamepadSaysWhetherAGestureHasAnywhereToPutIt() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"),
                pad(3, "CCC"));
        assertEquals(SimPads.State.UNCLAIMED_FREE,
                SimPads.state(pads.get(2), pads, pads.get(0), null));
        assertEquals(SimPads.State.UNCLAIMED_FULL,
                SimPads.state(pads.get(2), pads, pads.get(0), pads.get(1)));
    }

    // --- which controls the report names -----------------------------------

    @Test
    public void anUntouchedGamepadHasNothingToReport() {
        assertEquals(Collections.emptyList(), SimPads.offRest(new Gamepad()));
    }

    @Test
    public void aStickThatWasPushedAndLetGoIsNotReported() {
        Gamepad state = new Gamepad();
        state.left_stick_y = -0.00003f;
        state.right_stick_x = 0.04f;
        assertEquals("0.00003 of full scale is where a released stick sits",
                Collections.emptyList(), SimPads.offRest(state));
    }

    @Test
    public void aStickAndAButtonAreBothNamedWithTheirValues() {
        Gamepad state = new Gamepad();
        state.left_stick_y = -0.7344055f;
        state.a = true;
        assertEquals(Arrays.asList("left_stick_y=-0.73", "a=true"), SimPads.offRest(state));
    }

    @Test
    public void aTriggerIsReportedOnceItIsPastTheThreshold() {
        Gamepad state = new Gamepad();
        state.right_trigger = SimPads.OFF_REST;
        assertEquals("exactly at the threshold is still at rest",
                Collections.emptyList(), SimPads.offRest(state));
        state.right_trigger = 0.06f;
        assertEquals(Collections.singletonList("right_trigger=0.06"), SimPads.offRest(state));
    }

    @Test
    public void everyOneOfTheTwentyOneControlsCanBeReported() throws Exception {
        Gamepad state = new Gamepad();
        for (String name : SimArgs.AXES) {
            Gamepad.class.getField(name).setFloat(state, 1f);
        }
        for (String name : SimArgs.BUTTONS) {
            Gamepad.class.getField(name).setBoolean(state, true);
        }
        List<String> named = new ArrayList<>();
        for (String control : SimPads.offRest(state)) {
            named.add(control.substring(0, control.indexOf('=')));
        }
        assertEquals(21, named.size());
        assertTrue("every control the options can set: " + named,
                named.containsAll(SimArgs.CONTROLS));
    }

    // --- the claiming gesture ---------------------------------------------

    private static Gamepad pressing(boolean start, boolean a, boolean b) {
        Gamepad state = new Gamepad();
        state.start = start;
        state.a = a;
        state.b = b;
        return state;
    }

    @Test
    public void startAndAClaimsPlayerOneAndStartAndBClaimsPlayerTwo() {
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, false)));
        assertEquals(2, SimPads.claimedPlayer(pressing(true, false, true)));
    }

    @Test
    public void aButtonWithoutStartClaimsNothing() {
        assertEquals("A on its own", 0, SimPads.claimedPlayer(pressing(false, true, false)));
        assertEquals("B on its own", 0, SimPads.claimedPlayer(pressing(false, false, true)));
        assertEquals("Start on its own", 0, SimPads.claimedPlayer(pressing(true, false, false)));
    }

    @Test
    public void bothButtonsAtOnceClaimsPlayerOne() {
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, true)));
    }

    // --- what a gesture does to the two slots -----------------------------

    @Test
    public void aGestureFillsAnEmptySlotAndLeavesTheOtherAlone() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals(new SimGamepad.Pad[] {one, two},
                SimPads.displace(one, 1, null, two));
        assertArrayEquals(new SimGamepad.Pad[] {one, two},
                SimPads.displace(two, 2, one, null));
    }

    @Test
    public void theGamepadDisplacedMovesToTheOtherSlotWhenItIsFree() {
        SimGamepad.Pad held = pad(1, "AAA");
        SimGamepad.Pad arriving = pad(2, "BBB");
        assertArrayEquals("held moves out of gamepad1 and into gamepad2",
                new SimGamepad.Pad[] {arriving, held},
                SimPads.displace(arriving, 1, held, null));
    }

    @Test
    public void aThirdGamepadLeavesTheDisplacedOneWithNoSlot() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        SimGamepad.Pad three = pad(3, "CCC");
        assertArrayEquals("one is displaced and both slots are full",
                new SimGamepad.Pad[] {three, two},
                SimPads.displace(three, 1, one, two));
    }

    @Test
    public void aGamepadClaimingTheOtherSlotSwapsWithWhoeverIsThere() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals(new SimGamepad.Pad[] {two, one},
                SimPads.displace(one, 2, one, two));
        assertArrayEquals("and it leaves gamepad1 empty when nobody is in gamepad2",
                new SimGamepad.Pad[] {null, one},
                SimPads.displace(one, 2, one, null));
    }

    @Test
    public void claimingTheSlotAGamepadAlreadyHasChangesNothing() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals("a held Start and A is not a new gesture every 50 ms",
                new SimGamepad.Pad[] {one, two},
                SimPads.displace(one, 1, one, two));
        assertArrayEquals("and it does not put one gamepad in both slots",
                new SimGamepad.Pad[] {one, null},
                SimPads.displace(one, 1, one, null));
    }

    // --- writing the assignment down --------------------------------------

    private Path store(String... lines) throws IOException {
        Path file = folder.newFile("local.properties").toPath();
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    public void writingTheAssignmentLeavesEveryOtherLineAlone() throws IOException {
        Path file = store("# do not check this in", "sdk.dir=/Users/somebody/sdk");
        SimPads.write(file, "AAA", "BBB");
        assertEquals(Arrays.asList("# do not check this in",
                        "sdk.dir=/Users/somebody/sdk",
                        SimPads.KEY1 + "=AAA",
                        SimPads.KEY2 + "=BBB"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    public void writingTwiceReplacesTheLineRatherThanAddingASecond() throws IOException {
        Path file = store("sdk.dir=/sdk");
        SimPads.write(file, "AAA", "BBB");
        SimPads.write(file, "CCC", "DDD");
        assertEquals(Arrays.asList("sdk.dir=/sdk",
                        SimPads.KEY1 + "=CCC",
                        SimPads.KEY2 + "=DDD"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        assertEquals("CCC", SimPads.read(file).get(SimPads.KEY1));
    }

    @Test
    public void anUnclaimedPlayerLeavesNoLine() throws IOException {
        Path file = store("sdk.dir=/sdk", SimPads.KEY1 + "=AAA", SimPads.KEY2 + "=BBB");
        SimPads.write(file, "AAA", null);
        assertEquals(Arrays.asList("sdk.dir=/sdk", SimPads.KEY1 + "=AAA"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        assertNull(SimPads.read(file).get(SimPads.KEY2));
    }

    @Test
    public void aCommentedOutAssignmentIsNotReadAndIsNotEaten() throws IOException {
        Path file = store("#" + SimPads.KEY1 + "=OLD", "sdk.dir=/sdk");
        assertNull("a commented line is not a value", SimPads.read(file).get(SimPads.KEY1));
        SimPads.write(file, "AAA", null);
        assertEquals(Arrays.asList("#" + SimPads.KEY1 + "=OLD",
                        "sdk.dir=/sdk",
                        SimPads.KEY1 + "=AAA"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    public void aMissingFileIsNotAnError() {
        Path missing = folder.getRoot().toPath().resolve("nothing-here.properties");
        assertEquals(Collections.emptyMap(), SimPads.read(missing));
    }
}
