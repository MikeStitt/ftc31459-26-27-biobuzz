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
     */
    public void sticks(double forwardSpeed, double strafeLeftSpeed, double turnCcwSpeed) {
        double frontLeftPower = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        double frontRightPower = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        double backLeftPower = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        double backRightPower = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
        driveWheelsNow(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    }

    /**
     * Sends each of the four powers to its own motor, the slot named by {@link #FL}
     * and the others to the motor of that name.
     */
    @Override
    protected void writeWheels() {
        hardware.frontLeft.setPower(wheelPowers[FL]);
        hardware.frontRight.setPower(wheelPowers[FR]);
        hardware.backLeft.setPower(wheelPowers[BL]);
        hardware.backRight.setPower(wheelPowers[BR]);
    }
}
