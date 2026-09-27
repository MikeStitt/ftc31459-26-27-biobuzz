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
        // TODO 1: wrap the three numbers in a new DrivePowers(...) in its own
        //         variable, hand that to mix() into a second variable, hand that
        //         to normalized() into a third, and pass the four slots to
        //         setCommandedWheels(). One call to a line; nothing nested.
        //         Going through mix() is the point: the sticks and the follower
        //         then agree about which wheel does what.
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
        // TODO 2: fill each slot in, one line each, naming it with FL, FR, BL or
        //         BR. The four sums are the same ones L5HolonomicDriveTrain uses.
        return wheels;
    }

    /** Sends each of the four powers to its own motor, by the slot's own name. */
    @Override
    protected void writeWheels() {
        // TODO: send each slot of wheelPowers to its own motor, naming the slot
        //       with FL, FR, BL or BR and the motor through hardware:
        //       hardware.frontLeft.setPower(wheelPowers[FL]);  and the other three.
    }
}
