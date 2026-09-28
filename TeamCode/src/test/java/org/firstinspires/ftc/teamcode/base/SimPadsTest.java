package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The two parts of {@link SimPads} that can go wrong without a pad plugged in:
 * whether a stored serial is trusted, and whether writing the assignment leaves
 * the rest of somebody's {@code local.properties} alone.
 *
 * <p>The gesture itself needs a hand on a pad and is not here.
 */
public final class SimPadsTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static SimGamepad.Pad pad(int id, String serial) {
        return new SimGamepad.Pad(id, 0L, "pad " + id, serial);
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

    @Test
    public void noPadAtAllSaysSo() {
        assertEquals("No pad was plugged in after 60 s.", SimPads.verdict(true, true, 60));
        assertEquals("No pad was plugged in after 5 s.", SimPads.verdict(true, false, 5));
    }

    @Test
    public void aPadWithAPlayerSaysRunALesson() {
        assertEquals("Every pad has a player; run a lesson.", SimPads.verdict(false, true, 60));
    }

    @Test
    public void aPadLeftUnclaimedSaysItGaveUp() {
        assertEquals("Gave up after 60 s with a pad unclaimed.",
                SimPads.verdict(false, false, 60));
    }
}
