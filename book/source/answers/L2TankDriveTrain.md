# L2TankDriveTrain

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L2TankDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: call driveWheelsNow with four powers, in the order front left,
        //       front right, back left, back right. Both left wheels get
        //       leftSpeed and both right wheels get rightSpeed, so two of the
        //       four are the same number twice.
```

What the solutions line has there:

```java
        driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: send each of the four powers to its own motor, in the order they
        //       arrive: frontLeft.setPower(wheels[0]); and so on for the other
        //       three. Getting two of them the wrong way round makes the robot
        //       turn when it should drive, and nothing says so out loud, which is
        //       why this method has a test of its own.
```

What the solutions line has there:

```java
        frontLeft.setPower(wheels[0]);
        frontRight.setPower(wheels[1]);
        backLeft.setPower(wheels[2]);
        backRight.setPower(wheels[3]);
```
