package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * The drivetrain for L2: two sticks, four wheels.
 *
 * <p>This is the first drivetrain, and every later one is built the same way.
 * {@code extends CorbelsDriveTrain} means it starts with everything
 * {@link CorbelsDriveTrain} already has -- the four motors, which way each one
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
public class L2TankDriveTrain extends CorbelsDriveTrain {

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
        // TODO: call driveWheelsNow with four powers, in the order front left,
        //       front right, back left, back right. Both left wheels get
        //       leftSpeed and both right wheels get rightSpeed, so two of the
        //       four are the same number twice.
    }

    /**
     * Sends each of the four powers to its own motor. They always arrive in the
     * order front left, front right, back left, back right.
     */
    @Override
    protected void writeWheels(double[] wheels) {
        // TODO: send each of the four powers to its own motor, in the order they
        //       arrive: frontLeft.setPower(wheels[0]); and so on for the other
        //       three. Getting two of them the wrong way round makes the robot
        //       turn when it should drive, and nothing says so out loud, which is
        //       why this method has a test of its own.
    }
}
