package org.firstinspires.ftc.teamcode.base;

/**
 * Teleops for testing the simulator itself, built only out of {@code base}.
 *
 * <p>A test of the simulator that drove a lesson would fail on the lessons
 * branch, where the lessons are blanks, and a failure outside the
 * {@code lessons} package there is a defect rather than the point. These two
 * are complete on both branches. That a real lesson's sticks move the
 * simulated robot is {@code LessonsTest.l2_theSticksMoveTheSimulatedRobot},
 * where a blank L2 failing is expected.
 */
final class SimOpModes {

    private SimOpModes() {
    }

    /** Tank sticks onto the drivetrain: left stick to the left pair. */
    public static class Tank extends CorbelsTeleOp {

        private CorbelsMecanum drivetrain;

        @Override
        public void init() {
            initBefore();
            drivetrain = new CorbelsMecanum(hardware);
            initAfter(drivetrain);
        }

        @Override
        public void loop() {
            loopBefore();
            double left = -gamepad1.left_stick_y;
            double right = -gamepad1.right_stick_y;
            drivetrain.setCommandedWheels(left, right, left, right);
            loopAfter();
        }
    }

    /**
     * Prints the A button and its edge for the first two loops.
     *
     * <p>For watching what {@code Gamepad.copy} does to a control held from
     * before the run starts: the press edge arrives on the first loop and not
     * on the second, with the button still down. Bounded to two loops so a run
     * of it is one pair of lines rather than a scrolling screen.
     */
    public static class Edges extends CorbelsTeleOp {

        private int loops;

        @Override
        public void init() {
            initBefore();
            initAfter(new CorbelsMecanum(hardware));
        }

        @Override
        public void loop() {
            loopBefore();
            if (loops < 2) {
                System.out.println("loop " + loops + ": a=" + gamepad1.a
                        + " aWasPressed=" + gamepad1.aWasPressed());
            }
            loops++;
            loopAfter();
        }
    }

    /** Nothing but a drivetrain the follower can reach. */
    public static class Driven extends CorbelsTeleOp {

        @Override
        public void init() {
            initBefore();
            initAfter(new CorbelsMecanum(hardware));
        }

        @Override
        public void loop() {
            loopBefore();
            loopAfter();
        }
    }
}
