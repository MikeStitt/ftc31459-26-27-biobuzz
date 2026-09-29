package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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

    // --- whether a stored serial is trusted -------------------------------

    @Test
    public void oneSerialNamingOnePadIsTheAssignment() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        assertSame(pads.get(1), SimPads.onlyPadWithSerial(pads, "BBB"));
    }

    @Test
    public void aSerialNoPadAnswersToIsNotTrusted() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertNull("the pad it named is unplugged", SimPads.onlyPadWithSerial(pads, "BBB"));
    }

    @Test
    public void aSerialTwoPadsAnswerToIsNotTrusted() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "SAME"), pad(2, "SAME"));
        assertNull("two pads, one serial, no way to tell them apart",
                SimPads.onlyPadWithSerial(pads, "SAME"));
    }

    @Test
    public void aPadWithNoSerialIsNeverMatched() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, null));
        assertNull(SimPads.onlyPadWithSerial(pads, "AAA"));
        assertNull("and an empty stored serial matches nothing either",
                SimPads.onlyPadWithSerial(pads, ""));
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
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, false), true, true));
        assertEquals(2, SimPads.claimedPlayer(pressing(true, false, true), true, true));
    }

    @Test
    public void aButtonWithoutStartClaimsNothing() {
        assertEquals("A on its own", 0,
                SimPads.claimedPlayer(pressing(false, true, false), true, true));
        assertEquals("B on its own", 0,
                SimPads.claimedPlayer(pressing(false, false, true), true, true));
        assertEquals("Start on its own", 0,
                SimPads.claimedPlayer(pressing(true, false, false), true, true));
    }

    @Test
    public void aPlayerThatAlreadyHasApadCannotBeClaimedAgain() {
        assertEquals(0, SimPads.claimedPlayer(pressing(true, true, false), false, true));
        assertEquals(0, SimPads.claimedPlayer(pressing(true, false, true), true, false));
    }

    @Test
    public void bothButtonsAtOnceClaimsPlayerOne() {
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, true), true, true));
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
