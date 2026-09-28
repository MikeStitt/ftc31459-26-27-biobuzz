package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/** The infrastructure students don't see: heading hold and buttons. */
public class BaseTest {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 1e-9;

    private SimRobot robot;
    private Follower follower;

    @Before
    public void setUp() {
        robot = new SimRobot();
        follower = robot.follower;
        Scheduler.reset();
    }

    // ---------------------------------------------------------- HeadingHold

    @Test
    public void headingHoldPassesTheStickThroughWhileSteering() {
        HeadingHold hold = new HeadingHold(Controller.proportional(2.0));
        follower.setPose(POSES.of(0, 0, 0));
        follower.update();
        assertEquals(0.7, hold.turn(follower, 0.7), EPS);
        assertNull("no target while the driver steers", hold.target());
    }

    @Test
    public void headingHoldCapturesTheHeadingWhenTheStickIsReleased() {
        HeadingHold hold = new HeadingHold(Controller.proportional(2.0));
        follower.setPose(POSES.of(0, 0, 45));
        follower.update();
        hold.turn(follower, 0.7);                       // steering
        double turn = hold.turn(follower, 0.0);         // released
        assertEquals(Math.toRadians(45), hold.target(), 1e-6);
        assertEquals("already on target", 0.0, turn, 1e-6);
    }

    @Test
    public void headingHoldPushesBackWhenTheRobotDrifts() {
        HeadingHold hold = new HeadingHold(Controller.proportional(2.0));
        follower.setPose(POSES.of(0, 0, 0));
        follower.update();
        hold.turn(follower, 0.0);                       // captures 0
        follower.setPose(POSES.of(0, 0, -10));          // drifted clockwise
        follower.update();
        assertTrue("turns back counterclockwise", hold.turn(follower, 0.0) > 0);
        follower.setPose(POSES.of(0, 0, 10));
        follower.update();
        assertTrue("turns back clockwise", hold.turn(follower, 0.0) < 0);
    }

    @Test
    public void headingHoldTakesTheShortWayRoundAcrossTheWrap() {
        HeadingHold hold = new HeadingHold(Controller.proportional(2.0));
        follower.setPose(POSES.of(0, 0, 179));
        follower.update();
        hold.turn(follower, 0.0);                       // holding +179 deg
        follower.setPose(POSES.of(0, 0, -179));         // 2 degrees away, the short way
        follower.update();
        double turn = hold.turn(follower, 0.0);
        assertTrue("small correction, not a full lap", Math.abs(turn) < 0.5);
        assertTrue("and in the right direction", turn < 0);
    }

    @Test
    public void wrapKeepsAnglesInRange() {
        assertEquals(0.0, HeadingHold.wrap(2 * Math.PI), 1e-9);
        assertEquals(-Math.PI / 2, HeadingHold.wrap(3 * Math.PI / 2), 1e-9);
        assertEquals(0.1, HeadingHold.wrap(0.1 + 4 * Math.PI), 1e-9);
    }

    @Test
    public void releasingForgetsTheTarget() {
        HeadingHold hold = new HeadingHold(Controller.proportional(2.0));
        follower.setPose(POSES.of(0, 0, 0));
        follower.update();
        hold.turn(follower, 0.0);
        hold.release();
        assertNull(hold.target());
    }

    // -------------------------------------------------------------- Buttons

    @Test
    public void whenPressedRunsOncePerPress() {
        AtomicInteger runs = new AtomicInteger();
        boolean[] pressed = {false};
        Buttons buttons = new Buttons();
        buttons.whenPressed(() -> pressed[0], Commands.instant(runs::incrementAndGet));

        buttons.poll();
        Scheduler.execute();
        assertEquals(0, runs.get());

        pressed[0] = true;
        buttons.poll();
        Scheduler.execute();
        assertEquals(1, runs.get());

        buttons.poll();                 // still held: must not repeat
        Scheduler.execute();
        assertEquals(1, runs.get());

        pressed[0] = false;
        buttons.poll();
        pressed[0] = true;
        buttons.poll();
        Scheduler.execute();
        assertEquals("a second press runs it again", 2, runs.get());
    }

    @Test
    public void whenReleasedRunsOnTheWayUp() {
        AtomicInteger runs = new AtomicInteger();
        boolean[] pressed = {true};
        Buttons buttons = new Buttons();
        buttons.whenReleased(() -> pressed[0], Commands.instant(runs::incrementAndGet));
        buttons.poll();
        Scheduler.execute();
        assertEquals(0, runs.get());
        pressed[0] = false;
        buttons.poll();
        Scheduler.execute();
        assertEquals(1, runs.get());
    }

    @Test
    public void whileHeldCancelsOnRelease() {
        boolean[] pressed = {false};
        Command forever = Commands.infinite(() -> { });
        Buttons buttons = new Buttons();
        buttons.whileHeld(() -> pressed[0], forever);

        pressed[0] = true;
        buttons.poll();
        Scheduler.execute();
        assertTrue(Scheduler.isRunning(forever));

        pressed[0] = false;
        buttons.poll();
        Scheduler.execute();
        assertFalse("released: the command stops", Scheduler.isRunning(forever));
    }

    @Test
    public void bindingsAreIndependent() {
        AtomicInteger a = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        boolean[] first = {false};
        boolean[] second = {false};
        Buttons buttons = new Buttons()
                .whenPressed(() -> first[0], Commands.instant(a::incrementAndGet))
                .whenPressed(() -> second[0], Commands.instant(b::incrementAndGet));
        assertEquals(2, buttons.size());
        second[0] = true;
        buttons.poll();
        Scheduler.execute();
        assertEquals(0, a.get());
        assertEquals(1, b.get());
    }
}
