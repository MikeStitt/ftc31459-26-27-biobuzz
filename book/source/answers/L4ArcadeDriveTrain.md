# L4ArcadeDriveTrain

The blanks in this file, filled in from `solutions-03`:

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
