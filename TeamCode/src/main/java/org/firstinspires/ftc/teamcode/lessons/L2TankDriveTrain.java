package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * The drivetrain for L2: two sticks, four wheels.
 *
 * <p>This is the first drivetrain, and every later one is built the same way.
 * {@code extends LessonsDriveTrain} means it starts with everything
 * {@link LessonsDriveTrain} already has -- the four motors, which way each one
 * spins, and braking when the power goes to 0 -- and adds what is special about
 * this lesson. {@code super(hardware)} is how the motors get handed over.
 *
 * <p>One method is this lesson's own: {@link #sticks} turns the two stick
 * numbers into four wheel powers. Sending them to the motors is
 * {@link LessonsDriveTrain#writeWheels}, written once there and used by every
 * lesson after this one. Nothing else writes to these motors while L2 is
 * running, so what {@code sticks} asks for is what the wheels do.
 *
 * <p>Passes when: LessonsTest.l2b_theSticksDriveTheWheelsLikeATank
 */
public class L2TankDriveTrain extends LessonsDriveTrain {

    public L2TankDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Tank drive: the left stick runs both left wheels, the right stick runs
     * both right wheels. Powers are -1 to 1.
     *
     * <p>Push both sticks forward and the robot goes straight. Push one forward
     * and one back and it spins. That is the whole idea, and it lives here in
     * one place so every lesson that drives this way says the same thing.
     */
    public void sticks(double leftSpeed, double rightSpeed) {
        driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
    }
}
