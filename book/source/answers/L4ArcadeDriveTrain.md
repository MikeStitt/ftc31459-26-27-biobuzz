# L4ArcadeDriveTrain

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L4ArcadeDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: work out what each side has to do, then call driveWheelsNow the
        //       way L2 did. A counter-clockwise turn runs the left side
        //       backwards, so the turn number is subtracted on the left and
        //       added on the right.
        double leftSpeed = 0;
        double rightSpeed = 0;
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
