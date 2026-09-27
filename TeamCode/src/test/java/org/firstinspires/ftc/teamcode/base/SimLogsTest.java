package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * What {@link SimLogs} adds to a failure.
 *
 * <p>The rule itself is watched working by breaking an assertion on purpose,
 * which no passing test can do. What is checked here is the note it builds.
 */
public class SimLogsTest {

    @Test
    public void aRunThatLoggedNothingAddsNothing() {
        assertEquals("", SimLogs.note(OpModeHarness.logFolders.size()));
    }

    @Test
    public void aRunThatLoggedNamesTheFileAndItsSize() {
        int before = OpModeHarness.logFolders.size();
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        h.run(20, 0);
        String note = SimLogs.note(before);
        assertTrue(note, note.contains("AdvantageScope can open what ran:"));
        assertTrue(note, note.contains(".wpilog"));
        assertTrue(note, note.contains("bytes"));
        assertEquals("one run, one log", 1, SimLogs.logs(before).size());
        assertTrue("the log has something in it", SimLogs.logs(before).get(0).length() > 0);
    }
}
