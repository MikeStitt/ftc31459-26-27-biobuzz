# L2TankDriveTrain

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L2TankDriveTrain.java`

## TODO (L2p2)

What the lesson leaves blank:

```java
        // TODO (L2p2): call driveWheelsNow with four powers. The left stick runs both
        //       left wheels and the right stick runs both right wheels, so two of
        //       the four are the same number.
```

What the solutions line has there:

```java
        driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
```

## TODO (L2p2)

What the lesson leaves blank:

```java
        // TODO (L2p2): send each slot of wheelPowers to its own motor, naming the slot
        //       with FL, FR, BL or BR and the motor through hardware:
        //       hardware.frontLeft.setPower(wheelPowers[FL]);  and the other three.
```

What the solutions line has there:

```java
        hardware.frontLeft.setPower(wheelPowers[FL]);
        hardware.frontRight.setPower(wheelPowers[FR]);
        hardware.backLeft.setPower(wheelPowers[BL]);
        hardware.backRight.setPower(wheelPowers[BR]);
```
