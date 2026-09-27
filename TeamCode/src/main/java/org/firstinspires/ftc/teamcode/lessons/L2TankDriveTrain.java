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
 * <p>Two methods are this lesson's own. {@link #sticks} turns the two stick
 * numbers into four wheel powers, and {@link #writeWheels} sends them to the
 * motors. Nothing else writes to these motors while L2 is running, so what
 * {@code sticks} asks for is what the wheels do.
 *
 * <p>Passes when: LessonsTest.l2_theSticksDriveTheWheelsLikeATank
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

    /**
     * Sends each of the four powers to its own motor. They always arrive in the
     * order front left, front right, back left, back right.
     */
    @Override
    protected void writeWheels(double[] wheels) {
        frontLeft.setPower(wheels[0]);
        frontRight.setPower(wheels[1]);
        backLeft.setPower(wheels[2]);
        backRight.setPower(wheels[3]);
    }
}
