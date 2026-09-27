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
        // TODO (L2p2): call driveWheelsNow with four powers. The left stick runs both
        //       left wheels and the right stick runs both right wheels, so two of
        //       the four are the same number.
    }

    /**
     * Sends each of the four powers to its own motor, the slot named by {@link #FL}
     * and the others to the motor of that name.
     */
    @Override
    protected void writeWheels() {
        // TODO (L2p2): send each slot of wheelPowers to its own motor, naming the slot
        //       with FL, FR, BL or BR and the motor through hardware:
        //       hardware.frontLeft.setPower(wheelPowers[FL]);  and the other three.
    }
}
