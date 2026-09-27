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
        DrivePowers drivePowers = new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        double[] mixed = mix(drivePowers);
        double[] scaled = normalized(mixed);
        setCommandedWheels(scaled[FL], scaled[FR], scaled[BL], scaled[BR]);
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

        double[] wheels = new double[4];
        wheels[FL] = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        wheels[FR] = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        wheels[BL] = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        wheels[BR] = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
        return wheels;
    }

    /** Sends each of the four powers to its own motor, by the slot's own name. */
    @Override
    protected void writeWheels() {
        hardware.frontLeft.setPower(wheelPowers[FL]);
        hardware.frontRight.setPower(wheelPowers[FR]);
        hardware.backLeft.setPower(wheelPowers[BL]);
        hardware.backRight.setPower(wheelPowers[BR]);
    }
}
