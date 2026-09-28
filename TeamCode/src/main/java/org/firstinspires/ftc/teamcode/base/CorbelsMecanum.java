package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.drivetrain.DrivePowers;

/**
 * A finished mecanum drivetrain, for code that is not a lesson.
 *
 * <p>Everything a drivetrain does is in {@link CorbelsDriveTrain}. This fills in
 * the one part that class leaves open, with the mixing Pedro's own
 * {@code Mecanum} uses, so autonomous behaves exactly as it did before we
 * replaced Pedro's drivetrain with our own.
 *
 * <p>It is deliberately a separate file from the lessons' own drivetrain, and
 * deliberately the same four lines. A lesson's copy has blanks in it for a
 * student to fill, so the tuners and the system identification tools cannot
 * depend on it.
 */
public class CorbelsMecanum extends CorbelsDriveTrain {

    public CorbelsMecanum(RobotHardware hardware) {
        super(hardware);
    }

    /** Pedro's mixing, unchanged: forward, strafe and turn into four wheels. */
    @Override
    protected double[] mix(DrivePowers powers) {
        double forward = powers.forward();
        double strafe = powers.strafe();
        double turn = powers.turn();
        double[] wheels = new double[4];
        wheels[FL] = forward - strafe - turn;
        wheels[FR] = forward + strafe + turn;
        wheels[BL] = forward + strafe - turn;
        wheels[BR] = forward - strafe + turn;
        return wheels;
    }
}
