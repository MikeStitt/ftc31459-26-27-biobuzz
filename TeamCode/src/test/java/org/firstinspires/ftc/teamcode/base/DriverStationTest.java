package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Tracker} owns the only flush of the Driver Station. These say what a
 * lesson sees when it obeys that and what the driver sees when it does not.
 */
public class DriverStationTest {

    /** Prints, and lets the end of the loop send it. */
    public static class Polite extends CorbelsTeleOp {
        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() {
            loopBefore();
            Tracker.printToDs("Ready");
            Tracker.printToDs("Speed  %.1f in/s", 12.5);
            loopAfter();
        }
    }

    /** Flushes in the middle of its own loop. */
    public static class FlushesItself extends CorbelsTeleOp {
        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() {
            loopBefore();
            Tracker.printToDs("Ready");
            telemetry.update();
            loopAfter();
        }
    }

    @Test
    public void whatALessonPrintsReachesTheDriver() {
        OpModeHarness h = new OpModeHarness(new Polite());
        h.init();
        h.start();
        h.loop();

        assertTrue(h.driverStation.text(), h.driverStation.shows("Ready"));
        assertTrue(h.driverStation.text(), h.driverStation.shows("Speed  12.5 in/s"));
    }

    @Test
    public void aMidLoopFlushLeavesTheDriverAnEmptyScreen() {
        OpModeHarness h = new OpModeHarness(new FlushesItself());
        h.init();
        h.start();
        h.loop();

        // The middle flush sent the lines and emptied the buffer, so the flush
        // at the end of the loop had nothing left to send and replaced the
        // screen with nothing.
        assertFalse("Ready survived the second flush", h.driverStation.shows("Ready"));
        assertEquals("the driver sees nothing at all", "", h.driverStation.text());
    }

    /**
     * The SDK declares {@code public Telemetry telemetry} on {@code OpMode}, so
     * it cannot be renamed, hidden or deprecated, and a copied tutorial brings
     * it back without anything complaining. This is what complains.
     *
     * <p>Three files may still touch it. {@code Tracker} owns the flush.
     * {@code logcheck/} tests the flight log library itself, including a
     * deliberate crash and a run that never closes, so it keeps its own
     * telemetry. {@code pedro/procedures/} is Pedro's tuning code, which runs
     * on Pedro's own {@code Procedure} rather than on {@code CorbelsOpMode} and
     * never reaches a {@code Tracker}.
     */
    @Test
    public void nothingButTheTrackerReachesTheDriverStationDirectly() throws Exception {
        Path baseDirectory = HardwareRulesTest.sourceRoot();
        assertNotNull("could not find the sources to scan", baseDirectory);

        Pattern directTelemetryPattern = Pattern.compile("(^|[^\\w.])telemetry\\s*[.=]");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(baseDirectory)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java"))
                    .collect(Collectors.toList())) {
                String path = file.toString();
                if (path.endsWith("Tracker.java")) continue;
                if (path.contains("logcheck")) continue;
                if (path.contains("procedures")) continue;
                String code = HardwareRulesTest.withoutComments(
                        new String(Files.readAllBytes(file), "UTF-8"));
                for (String line : code.split("\n")) {
                    if (line.trim().startsWith("import ")) continue;
                    Matcher m = directTelemetryPattern.matcher(line);
                    if (m.find()) {
                        offenders.add(file.getFileName() + ": " + line.trim());
                    }
                }
            }
        }
        if (!offenders.isEmpty()) {
            fail("print with Tracker.printToDs instead -- " + String.join("; ", offenders));
        }
    }
}
