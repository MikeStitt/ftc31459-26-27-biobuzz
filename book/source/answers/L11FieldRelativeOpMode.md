# L11FieldRelativeOpMode

The blanks in this file, filled in from `e74ab6c`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L11FieldRelativeOpMode.java`

## TODO (L11)

What the lesson leaves blank:

```java
        // TODO (L11): the same three sticks as L5, but through
        //       drivetrain.fieldRelative(...) instead of drivetrain.sticks(...).
        //       The heading goes in first, and the drivetrain cannot get it
        //       itself: follower.pose().heading() is where it comes from.
        //       Try L5's version with the robot turned 180 degrees first, so you
        //       can feel the difference.
```

What the solutions line has there:

```java
        drivetrain.fieldRelative(follower.pose().heading(),
                -gamepad1.left_stick_y,      // away from the driver
                -gamepad1.left_stick_x,      // to the driver's left
                -gamepad1.right_stick_x);    // counter-clockwise
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
