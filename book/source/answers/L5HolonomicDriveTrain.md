# L5HolonomicDriveTrain

The blanks in this file, filled in from `solutions-sim-gamepad`:

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
