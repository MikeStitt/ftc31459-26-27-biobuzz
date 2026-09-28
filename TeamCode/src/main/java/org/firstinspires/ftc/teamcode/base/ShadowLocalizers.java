package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;


import java.util.ArrayList;
import java.util.List;

/**
 * Runs extra localizers alongside the real one and logs what each of them
 * thinks. Nothing here steers the robot -- they only watch.
 *
 * <p>Each one publishes {@code Localizer/<name>/x_in}, {@code /y_in} and
 * {@code /heading_deg}, so two answers to the same question can be graphed
 * against each other while you drive.
 */
public final class ShadowLocalizers {

    /** One localizer being watched, and whether this class is the one driving it. */
    private static final class ShadowLocalizer {
        final String name;
        final Localizer localizer;
        final boolean driven;

        ShadowLocalizer(String name, Localizer localizer, boolean driven) {
            this.name = name;
            this.localizer = localizer;
            this.driven = driven;
        }
    }

    private final List<ShadowLocalizer> entries = new ArrayList<>();

    /** A localizer nothing else updates: this class drives it and publishes it. */
    public ShadowLocalizers add(String name, Localizer localizer) {
        entries.add(new ShadowLocalizer(name, localizer, true));
        return this;
    }

    /**
     * A localizer somebody else already updates: published under a name, and
     * left alone otherwise.
     *
     * <p>The live localizer is the case. The follower updates it once a loop, and
     * updating it again here would integrate the same movement twice.
     */
    public ShadowLocalizers publishOnly(String name, Localizer localizer) {
        entries.add(new ShadowLocalizer(name, localizer, false));
        return this;
    }

    /** Puts every localizer this class drives at the pose given. */
    public void setPose(Pose pose) {
        for (ShadowLocalizer e : entries) {
            if (e.driven) e.localizer.setPose(pose);
        }
    }

    /** Call once per loop: updates the ones driven here and publishes them all. */
    public void update() {
        for (ShadowLocalizer e : entries) {
            if (e.driven) e.localizer.update();
            Pose p = e.localizer.pose();
            Tracker.publish("Localizer/" + e.name + "/x_in", p.x());
            Tracker.publish("Localizer/" + e.name + "/y_in", p.y());
            Tracker.publish("Localizer/" + e.name + "/heading_deg", Math.toDegrees(p.heading()));
        }
    }

    /** How far one localizer's answer is from another, in inches. */
    public static double distance(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    public int size() {
        return entries.size();
    }
}
