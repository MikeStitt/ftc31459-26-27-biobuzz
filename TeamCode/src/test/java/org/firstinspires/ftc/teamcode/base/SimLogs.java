package org.firstinspires.ftc.teamcode.base;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds to a failure the path of the flight log the test left, so the run can
 * be opened in AdvantageScope. One line in a test class turns it on:
 *
 * <pre>
 * &#64;Rule
 * public final SimLogs logs = new SimLogs();
 * </pre>
 *
 * <p>The path goes into the failure message rather than to standard output,
 * because Gradle keeps a test's output in the HTML report and shows only the
 * failure on the console.
 *
 * <p>A test that fails does not reach {@code stop()}, so the log it leaves is
 * whatever had been flushed by then rather than a closed file. That is enough
 * to watch the robot up to the moment the assertion went wrong.
 *
 * <p>Passes when: SimLogsTest.
 */
public final class SimLogs implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                int before = OpModeHarness.logFolders.size();
                try {
                    base.evaluate();
                } catch (AssertionError e) {
                    closeLog();
                    throw new AssertionError(e.getMessage() + note(before), e);
                }
            }
        };
    }

    /**
     * Closes the flight log, so the file holds the run rather than the header.
     * A failing test never reaches {@code stop()}, which is what closes it
     * normally, and an unclosed log is 0 bytes on disk. The next OpMode to
     * start opens a new one, so closing here costs the tests after this one
     * nothing.
     */
    static void closeLog() {
        try {
            Tracker.close();
        } catch (RuntimeException ignored) {
            // The log is what is being recovered; failing to close it must not
            // replace the assertion the test already failed on.
        }
    }

    /** What to add to a failure message: nothing, if nothing was logged. */
    static String note(int since) {
        List<File> found = logs(since);
        if (found.isEmpty()) return "";
        StringBuilder note = new StringBuilder("\nAdvantageScope can open what ran:");
        for (File log : found) {
            note.append("\n  ").append(log).append(" (").append(log.length()).append(" bytes)");
        }
        return note.toString();
    }

    /** The .wpilog files under every log folder made since the index given. */
    static List<File> logs(int since) {
        List<File> found = new ArrayList<>();
        List<File> folders = OpModeHarness.logFolders;
        for (File folder : folders.subList(Math.min(since, folders.size()), folders.size())) {
            File[] files = folder.listFiles((d, n) -> n.endsWith(".wpilog"));
            if (files == null) continue;
            for (File file : files) {
                found.add(file);
            }
        }
        return found;
    }
}
