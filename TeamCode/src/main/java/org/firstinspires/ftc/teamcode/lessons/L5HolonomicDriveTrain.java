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
public class L5HolonomicDriveTrain extends CorbelsDriveTrain {

    public L5HolonomicDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Holonomic drive, relative to the robot's own front.
     * {@code forwardSpeed} drives, {@code strafeLeftSpeed} slides towards the
     * robot's left, {@code turnCcwSpeed} spins counter-clockwise. All -1 to 1,
     * in the directions {@link CorbelsDriveTrain} sets out.
     *
     * <p>Asking for all three at once wants more than a motor can give, and
     * {@link CorbelsDriveTrain#normalized} sorts that out.
     */
    public void sticks(double forwardSpeed, double strafeLeftSpeed, double turnCcwSpeed) {
        // TODO: call driveWheelsNow with the four lines from the comment above
        //       this method, in the order front left, front right, back left,
        //       back right. Each one is the three numbers added or subtracted --
        //       the signs are what make a wheel push sideways one way or the
        //       other.
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
