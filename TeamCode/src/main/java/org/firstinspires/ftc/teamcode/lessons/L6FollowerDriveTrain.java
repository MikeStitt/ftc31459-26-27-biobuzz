package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * The drivetrain the path follower holds, from L6 onwards.
 *
 * <p>Up to L5 a drivetrain was the only thing writing to the motors, and it
 * wrote them the moment the lesson asked. From here the follower is holding it,
 * which is what lets the follower know where the robot is, hold a heading and
 * run a path. Two numbers change hands rather than one:
 *
 * <ul>
 *   <li>{@link #sticks} is the driver asking. It mixes the three numbers and
 *       commands the four wheels, so the driver still wins.
 *   <li>{@link #mix} is the follower asking, which is what happens from L8 once
 *       the wheels are handed back.
 * </ul>
 *
 * <p>Both go through the same four lines, so the robot that strafes correctly
 * with the sticks is the robot that follows a path correctly.
 *
 * <p>Passes when: L6FollowerDriveTrainTest (all of it)
 */
public class L6FollowerDriveTrain extends LessonsDriveTrain {

    public L6FollowerDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * The driver's three numbers, mixed and sent to the wheels, while the
     * follower is holding this drivetrain. All -1 to 1, in the directions
     * {@link LessonsDriveTrain} sets out.
     */
    public void sticks(double forwardSpeed, double strafeLeftSpeed, double turnCcwSpeed) {
        double[] wheels = normalized(
                mix(new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed)));
        setCommandedWheels(wheels[0], wheels[1], wheels[2], wheels[3]);
    }

    /**
     * Three numbers, as four wheel powers.
     *
     * <p>{@code powers.strafe()} is positive towards the robot's left and
     * {@code powers.turn()} is positive counter-clockwise, the same as L5. See
     * {@link LessonsDriveTrain} for where those directions come from.
     */
    @Override
    protected double[] mix(DrivePowers powers) {
        double forwardSpeed = powers.forward();
        double strafeLeftSpeed = powers.strafe();
        double turnCcwSpeed = powers.turn();

        return new double[]{
                forwardSpeed - strafeLeftSpeed - turnCcwSpeed,      // front left
                forwardSpeed + strafeLeftSpeed + turnCcwSpeed,      // front right
                forwardSpeed + strafeLeftSpeed - turnCcwSpeed,      // back left
                forwardSpeed - strafeLeftSpeed + turnCcwSpeed};     // back right
    }

    /** Sends each of the four powers to its own motor, in that same order. */
    @Override
    protected void writeWheels(double[] wheels) {
        frontLeft.setPower(wheels[0]);
        frontRight.setPower(wheels[1]);
        backLeft.setPower(wheels[2]);
        backRight.setPower(wheels[3]);
    }
}
