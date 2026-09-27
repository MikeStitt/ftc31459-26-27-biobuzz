# L4ArcadeDriveTrain

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L4ArcadeDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: turn the two numbers into a left speed and a right speed, each
        //       into its own variable, and hand them to driveWheelsNow the way
        //       L2TankDriveTrain does. Turning counter-clockwise means the left
        //       side goes slower and the right side faster.
```

What the solutions line has there:

```java
        double leftSpeed = forwardSpeed - turnCcwSpeed;
        double rightSpeed = forwardSpeed + turnCcwSpeed;
        driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
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
