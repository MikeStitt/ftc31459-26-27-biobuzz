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

    /** Tank sticks onto the wheels: left stick to the left pair. */
    public static class Tank extends CorbelsTeleOp {

        private CorbelsMecanum wheels;

        @Override
        public void init() {
            initBefore();
            wheels = new CorbelsMecanum(hardware);
            initAfter(wheels);
        }

        @Override
        public void loop() {
            loopBefore();
            double left = -gamepad1.left_stick_y;
            double right = -gamepad1.right_stick_y;
            wheels.setCommandedWheels(left, right, left, right);
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
