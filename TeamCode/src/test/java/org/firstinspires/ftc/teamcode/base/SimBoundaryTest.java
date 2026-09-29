package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The simulator does not reach the robot.
 *
 * <p>Everything the simulator is made of lives in the unit test source set, so
 * the robot's APK cannot contain it and neither can the gamepad library it
 * loads. What holds that is the placement plus this test: no file the APK is
 * built from names any of those classes.
 *
 * <p>The list of names is read off the test sources rather than written down
 * here, so a class added to the simulator is covered without this test being
 * edited.
 */
public final class SimBoundaryTest {

    @Test
    public void nothingTheRobotIsBuiltFromNamesASimulatorClass() throws Exception {
        List<String> simulator = classesUnderTest();
        assertTrue("found no test classes to look for", simulator.size() > 20);
        Path robot = HardwareRulesTest.sourceRoot();
        assertNotNull("could not find the sources the robot is built from", robot);

        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(robot)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java"))
                    .collect(Collectors.toList())) {
                String code = HardwareRulesTest.withoutComments(
                        new String(Files.readAllBytes(file), "UTF-8"));
                for (String name : simulator) {
                    if (Pattern.compile("\\b" + name + "\\b").matcher(code).find()) {
                        offenders.add(file.getFileName() + " names " + name);
                    }
                }
            }
        }
        if (!offenders.isEmpty()) {
            fail(String.join("; ", offenders));
        }
    }

    /** Every class in the unit test source set's own {@code base} package. */
    private static List<String> classesUnderTest() throws Exception {
        for (String candidate : new String[] {
                "src/test/java/org/firstinspires/ftc/teamcode/base",
                "TeamCode/src/test/java/org/firstinspires/ftc/teamcode/base"}) {
            Path path = Paths.get(candidate);
            if (!Files.isDirectory(path)) {
                continue;
            }
            try (Stream<Path> files = Files.list(path)) {
                return files.map(f -> f.getFileName().toString())
                        .filter(n -> n.endsWith(".java"))
                        .map(n -> n.substring(0, n.length() - ".java".length()))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
