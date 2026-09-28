package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Every device this code touches, looked up once.
 *
 * <p>Built in {@code init()}, before the match starts, so a name that doesn't
 * match the Robot Controller configuration fails where someone can read the
 * error -- not halfway through a match. Nothing else calls
 * {@link HardwareMap#get} afterwards, and no device name appears outside
 * {@link Constants}.
 *
 * <p>The drivetrain's names, directions and brake mode arrive together in a
 * {@link MecanumConfig}, which is kept in {@link #mecanumConfig} for the
 * drivetrains to read: {@code hardware.mecanumConfig.frontLeftDirection.get()}.
 * It is the same holder Pedro's own {@code Mecanum} takes and the same one
 * AutoTune generates, so a tuned value reaches our drivetrains as well as
 * Pedro's.
 */
public final class RobotHardware {

    public final DcMotorEx frontLeft;
    public final DcMotorEx frontRight;
    public final DcMotorEx backLeft;
    public final DcMotorEx backRight;
    public final IMU imu;

    /** The drivetrain's configuration, read live by whoever needs a value. */
    public final MecanumConfig mecanumConfig;

    /**
     * What the battery is giving. Not looked up by name: the SDK offers every
     * voltage sensor as a group, and the first is the Control Hub's own.
     */
    public final VoltageSensor battery;

    /** Looks up each motor by the name the config gives it. */
    public RobotHardware(HardwareMap map, MecanumConfig mecanumConfig) {
        this(map.get(DcMotorEx.class, mecanumConfig.frontLeftName.get()),
                map.get(DcMotorEx.class, mecanumConfig.frontRightName.get()),
                map.get(DcMotorEx.class, mecanumConfig.backLeftName.get()),
                map.get(DcMotorEx.class, mecanumConfig.backRightName.get()),
                map.get(IMU.class, Constants.imuName),
                map.voltageSensor.iterator().next(),
                mecanumConfig);
    }

    /** For tests, which supply their own fakes and their own config. */
    public RobotHardware(DcMotorEx frontLeft, DcMotorEx frontRight,
                         DcMotorEx backLeft, DcMotorEx backRight, IMU imu,
                         MecanumConfig mecanumConfig) {
        this(frontLeft, frontRight, backLeft, backRight, imu, null, mecanumConfig);
    }

    /** For tests, which supply their own fakes and their own config. */
    public RobotHardware(DcMotorEx frontLeft, DcMotorEx frontRight,
                         DcMotorEx backLeft, DcMotorEx backRight, IMU imu,
                         VoltageSensor battery, MecanumConfig mecanumConfig) {
        this.battery = battery;
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;
        this.imu = imu;
        this.mecanumConfig = mecanumConfig;
    }

    /** What the battery is giving, or a nominal 12 V if there is no sensor. */
    public double batteryVolts() {
        return battery == null ? 12.0 : battery.getVoltage();
    }
}
