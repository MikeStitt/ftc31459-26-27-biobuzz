package org.firstinspires.ftc.teamcode.base;

import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.WPIUtilJNI;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Puts WPILib's NetworkTables native libraries where the JVM can load them.
 *
 * <p>The published {@code -jni} jars carry one library each and nothing that
 * finds it. WPILib's own loaders do not work outside a GradleRIO build:
 * {@code RuntimeLoader} looks only along {@code java.library.path} and fails
 * with {@code no ntcorejni in java.library.path}, and
 * {@code CombinedRuntimeLoader} reads a {@code ResourceInformation} json that
 * GradleRIO generates and these jars do not contain, failing with
 * {@code argument "src" is null}. So the library is copied out of the jar to a
 * temporary file and loaded by name, which is all either of those would have
 * done.
 *
 * <p>Order matters: ntcore's library needs wpiutil's to be loaded already.
 *
 * <p>Passes when: SimPublisherTest.theServerStartsAndListens
 */
final class NtNatives {

    private static boolean loaded;

    private NtNatives() {
    }

    /** Loads both libraries, once per JVM. */
    static synchronized void load() {
        if (loaded) return;
        // Stop the JNI classes trying to find the libraries themselves.
        WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
        NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
        loadLibrary("wpiutiljni");
        loadLibrary("ntcorejni");
        loaded = true;
    }

    private static void loadLibrary(String name) {
        String resource = directory() + fileName(name);
        try (InputStream in = NtNatives.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("not on the test classpath: " + resource
                        + " -- check the classifier in TeamCode/build.gradle matches this machine");
            }
            Path file = Files.createTempDirectory("ntjni").resolve(fileName(name));
            file.toFile().deleteOnExit();
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            System.load(file.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new UncheckedIOException("could not unpack " + resource, e);
        }
    }

    /** Where WPILib puts the library inside the jar, by platform. */
    private static String directory() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean arm = arch.contains("aarch64") || arch.contains("arm64");
        if (os.contains("mac")) return "/osx/universal/";
        if (os.contains("win")) return arm ? "/windows/arm64/" : "/windows/x86-64/";
        return arm ? "/linux/arm64/" : "/linux/x86-64/";
    }

    private static String fileName(String name) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) return "lib" + name + ".dylib";
        if (os.contains("win")) return name + ".dll";
        return "lib" + name + ".so";
    }
}
