# L5HolonomicDriveTrain

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L5HolonomicDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: work out each wheel's power, one named variable at a time, then
        //       hand the four to driveWheelsNow. Three numbers add up differently
        //       at each corner; the sources in this class's javadoc draw it.
```

What the solutions line has there:

```java
        double frontLeftPower = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        double frontRightPower = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        double backLeftPower = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        double backRightPower = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
        driveWheelsNow(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: send each slot of wheelPowers to its own motor, naming the slot
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
