package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Two ways to watch a localizer, and the difference between them: one this class
 * drives, and one somebody else drives.
 */
public class ShadowLocalizersTest {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** A localizer that moves one inch along x every time it is updated. */
    private static final class Odometer implements Localizer {
        int updates;
        private Pose pose = POSES.of(0, 0, 0);

        @Override public void setPose(Pose pose) { this.pose = pose; }
        @Override public MotionState state() { return MotionState.zero().withPose(pose); }
        @Override public void reset() { }

        @Override public void update() {
            updates++;
            pose = pose.withX(pose.x() + 1);
        }
    }

    private ShadowLocalizers shadowLocalizers;
    private Odometer ours;
    private Odometer somebodyElses;

    @Before
    public void setUp() {
        Tracker.begin(null);
        shadowLocalizers = new ShadowLocalizers();
        ours = new Odometer();
        somebodyElses = new Odometer();
        shadowLocalizers.add("ours", ours);
        shadowLocalizers.publishOnly("theirs", somebodyElses);
    }

    @After
    public void tearDown() {
        Tracker.close();
    }

    @Test
    public void onlyTheLocalizerThisClassDrivesIsUpdated() {
        shadowLocalizers.update();
        shadowLocalizers.update();
        assertEquals("driven here, so updated here", 2, ours.updates);
        assertEquals("driven elsewhere, so not updated again here", 0, somebodyElses.updates);
    }

    @Test
    public void bothArePublishedUnderTheirOwnNames() {
        somebodyElses.update();              // whoever owns it moves it
        shadowLocalizers.update();
        assertEquals(1.0, (Double) Tracker.values().get("Localizer/ours/x_in"), 1e-9);
        assertEquals("published, not driven",
                1.0, (Double) Tracker.values().get("Localizer/theirs/x_in"), 1e-9);
    }

    @Test
    public void aStartingPoseReachesOnlyWhatThisClassDrives() {
        shadowLocalizers.setPose(POSES.of(24, 48, 0));
        assertEquals(24.0, ours.state().pose().x(), 1e-9);
        assertEquals("whoever owns it decides where it starts",
                0.0, somebodyElses.state().pose().x(), 1e-9);
    }
}
