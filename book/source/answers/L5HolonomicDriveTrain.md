# L5HolonomicDriveTrain

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L5HolonomicDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: call driveWheelsNow with the four lines from the comment above
        //       this method, in the order front left, front right, back left,
        //       back right. Each one is the three numbers added or subtracted --
        //       the signs are what make a wheel push sideways one way or the
        //       other.
```

What the solutions line has there:

```java
        driveWheelsNow(
                forwardSpeed - strafeLeftSpeed - turnCcwSpeed,
                forwardSpeed + strafeLeftSpeed + turnCcwSpeed,
                forwardSpeed + strafeLeftSpeed - turnCcwSpeed,
                forwardSpeed - strafeLeftSpeed + turnCcwSpeed);
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
