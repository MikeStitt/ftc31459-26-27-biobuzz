package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * The drivetrain for L5: all four wheels mixed separately, so the robot can
 * slide sideways.
 *
 * <p>Mecanum wheels have rollers set at 45 degrees, and the four wheels are
 * handed so the rollers make an X across the robot. Spin all four forwards and
 * the sideways pushes cancel, so the robot drives forward. Spin one diagonal
 * pair forwards and the other pair backwards and the forward pushes cancel
 * instead, so the robot slides.
 *
 * <p>That is the whole trick, and it is these four lines:
 *
 * <pre>
 *   front left  = forwardSpeed - strafeLeftSpeed - turnCcwSpeed
 *   front right = forwardSpeed + strafeLeftSpeed + turnCcwSpeed
 *   back left   = forwardSpeed + strafeLeftSpeed - turnCcwSpeed
 *   back right  = forwardSpeed - strafeLeftSpeed + turnCcwSpeed
 * </pre>
 *
 * <p>Those are the same four lines the path follower uses, so a robot that
 * strafes correctly here will follow a path correctly later.
 *
 * <p>Passes when: LessonsTest.l5_holonomicCanStrafe
 */
public class L5HolonomicDriveTrain extends LessonsDriveTrain {

    public L5HolonomicDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Holonomic drive, relative to the robot's own front.
     * {@code forwardSpeed} drives, {@code strafeLeftSpeed} slides towards the
     * robot's left, {@code turnCcwSpeed} spins counter-clockwise. All -1 to 1,
     * in the directions {@link LessonsDriveTrain} sets out.
     *
     * <p>Asking for all three at once wants more than a motor can give, and
     * {@link LessonsDriveTrain#normalized} sorts that out.
     *
     * <p>Where the four signs come from, if the pattern is not obvious yet:
     * <ul>
     *   <li>Game Manual 0's Mecanum TeleOp tutorial, which writes the same four
     *       lines for two sticks and a turn:
     *       https://gm0.org/en/latest/docs/software/tutorials/mecanum-drive.html
     *   <li>Pedro's coordinate frame, which is where forward, strafe and turn point:
     *       https://pedropathing.com/docs/pathing/reference/coordinates
     *   <li>WPILib's {@code MecanumDrive}, the same arithmetic in another library:
     *       https://github.com/wpilibsuite/allwpilib
     * </ul>
     */
    public void sticks(double forwardSpeed, double strafeLeftSpeed, double turnCcwSpeed) {
        // TODO: work out each wheel's power, one named variable at a time, then
        //       hand the four to driveWheelsNow. Three numbers add up differently
        //       at each corner; the sources in this class's javadoc draw it.
    }

    /**
     * Sends each of the four powers to its own motor, the slot named by {@link #FL}
     * and the others to the motor of that name.
     */
    @Override
    protected void writeWheels() {
        // TODO: send each slot of wheelPowers to its own motor, naming the slot
        //       with FL, FR, BL or BR and the motor through hardware:
        //       hardware.frontLeft.setPower(wheelPowers[FL]);  and the other three.
    }
}
