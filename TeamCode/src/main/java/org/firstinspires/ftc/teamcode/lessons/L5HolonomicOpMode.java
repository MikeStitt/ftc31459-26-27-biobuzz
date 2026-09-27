package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L5: drivetrain drive. Now the robot can strafe.
 *
 * <p>Arcade used two numbers, forward and turn, and both sides of the robot did
 * the same thing. Mecanum wheels can do a third: slide sideways without turning.
 * That needs all four wheels mixed separately, which is what
 * {@link L5HolonomicDriveTrain#sticks} does.
 *
 * <p>The left stick now does two jobs at once -- push it to drive, push it
 * sideways to slide -- and the right stick still turns. Everything is relative
 * to the robot's own front: push the stick left and the robot slides towards its
 * own left, whichever way it happens to be facing. Making it slide towards the
 * driver's left instead comes in L11.
 *
 * <p>Passes when: LessonsTest.l5_holonomicCanStrafe
 */
@TeleOp(name = "L5 Holonomic", group = "Lessons")
public class L5HolonomicOpMode extends CorbelsTeleOp {

    private L5HolonomicDriveTrain drivetrain;

    @Override
    public void init() {
        initBefore();
        drivetrain = new L5HolonomicDriveTrain(hardware);
        initAfter();
    }

    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();

        // TODO: three numbers now, not two. left comes from the left stick's x
        //       axis; LEFT is positive and the stick reads positive to the RIGHT,
        //       so that one needs negating too. Log all three.
        double forwardSpeed = 0;
        double strafeLeftSpeed = 0;
        double turnCcwSpeed = 0;
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);

        loopAfter();
    }

    @Override
    public void stop() {
        drivetrain.stop();
        stopAfter();
    }
}
