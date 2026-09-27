package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * The drivetrain for L4: one number to go, one number to turn.
 *
 * <p>Tank asked the driver to keep two sticks matched to go straight, which is
 * harder than it sounds. Arcade asks for what the robot should do instead: how
 * fast forward, and how fast to spin. {@link #sticks} works out what each side
 * has to do.
 *
 * <p>Turning counter-clockwise -- to the driver's left -- means the left wheels
 * go backwards while the right wheels go forwards. So the turn number is
 * subtracted from the left side and added to the right.
 *
 * <p>Full forward and full turn together add up to more than a motor can give.
 * {@link LessonsDriveTrain#normalized} is where that gets sorted out, and this
 * lesson writes it.
 *
 * <p>Passes when: LessonsTest.l4_arcadeUsesOneStickToDriveAndOneToTurn
 */
public class L4ArcadeDriveTrain extends LessonsDriveTrain {

    public L4ArcadeDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Arcade drive. {@code forwardSpeed} is how fast to drive,
     * {@code turnCcwSpeed} is how fast to spin counter-clockwise, in the
     * directions {@link LessonsDriveTrain} sets out. Both are -1 to 1.
     */
    public void sticks(double forwardSpeed, double turnCcwSpeed) {
        // TODO: work out what each side has to do, then call driveWheelsNow the
        //       way L2 did. A counter-clockwise turn runs the left side
        //       backwards, so the turn number is subtracted on the left and
        //       added on the right.
        double leftSpeed = 0;
        double rightSpeed = 0;
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
