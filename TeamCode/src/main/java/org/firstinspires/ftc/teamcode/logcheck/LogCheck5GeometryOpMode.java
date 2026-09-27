package org.firstinspires.ftc.teamcode.logcheck;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import io.github.mikestitt.corbelsflightlog.FlightLog;
import io.github.mikestitt.corbelsflightlog.ftc.FtcFlightLog;
import io.github.mikestitt.corbelsflightlog.ftc.FtcGeometry;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Check 5: the geometry types, for looking at in AdvantageScope.
 *
 * <p>Drives an imaginary robot in a circle -- no hardware needed -- and logs it
 * as a Pose2d, a Pose3d, ChassisSpeeds and wheel speeds. If the configuration
 * has an IMU under the name in Constants, its real orientation is logged too.
 *
 * <p>Afterwards, in AdvantageScope: drag Circle/Pose onto the 2D field (it
 * should go round in a circle), Circle/Pose3d onto the 3D field, and look at
 * Circle/Speeds and Circle/Wheels in a table.
 */
@TeleOp(name = "Log 5: geometry", group = "LogCheck")
public class LogCheck5GeometryOpMode extends OpMode {

    private static final double METRE_IN_INCHES = 39.3700787401575;

    private FlightLog log;
    private IMU imu;
    private long startNs;

    @Override
    public void init() {
        try {
            imu = hardwareMap.get(IMU.class, Constants.imuName);
        } catch (Exception e) {
            imu = null;    // fine: the rest of the check does not need it
        }
        telemetry.addData("IMU", imu == null ? "not configured (that's OK)" : "found");
        telemetry.update();
    }

    @Override
    public void start() {
        log = FtcFlightLog.open(this);
        startNs = System.nanoTime();
    }

    @Override
    public void loop() {
        double t = (System.nanoTime() - startNs) / 1e9;

        // An imaginary robot going round a 24 inch circle in the middle of the
        // field, facing the way it travels. Pedro units: inches, corner origin.
        double xIn = 72 + 24 * Math.cos(t);
        double yIn = 72 + 24 * Math.sin(t);
        double heading = t + Math.PI / 2;
        log.pose("Circle/Pose", xIn, yIn, heading);

        // The same pose, a metre off the floor. Same Pedro coordinates, so the
        // library applies the same field rotation -- converting by hand here
        // left the 3D robot 90 degrees away from the 2D one.
        log.pose("Circle/Pose3d", xIn, yIn, METRE_IN_INCHES, heading);

        log.chassisSpeeds("Circle/Speeds", 24 * 0.0254, 0, 1.0);
        log.mecanumWheelSpeeds("Circle/Wheels", 1.0, 1.0, 0.9, 0.9);
        log.twist2d("Circle/Twist", 0.6, 0, 1.0);
        log.recordOutput("Circle/t", t);

        if (imu != null) {
            FtcGeometry.recordOutput(log, "imu/Rotation", imu.getRobotYawPitchRollAngles());
            FtcGeometry.heading(log, "imu/Heading", imu.getRobotYawPitchRollAngles());
        }
        log.endLoop();

        telemetry.addData("Log", log.status());
        telemetry.addData("t", "%.1f s", t);
        telemetry.addLine("AdvantageScope: Circle/Pose on 2D field, Circle/Pose3d on 3D.");
        telemetry.update();
    }

    @Override
    public void stop() {
        log.close();
    }
}
