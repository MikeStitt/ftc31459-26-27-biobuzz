# L11FieldRelativeOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L11FieldRelativeOpMode.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: same three numbers as lesson 5, but through
        //       Drive.fieldRelative(...) instead of Drive.holonomic(...).
        //       Try lesson 5's version with the robot turned 180 degrees first,
        //       so you can feel the difference.
```

What the solutions line has there:

```java
        Drive.fieldRelative(follower,
                -gamepad1.left_stick_y,      // away from the driver
                -gamepad1.left_stick_x,      // to the driver's left
                -gamepad1.right_stick_x);    // counter-clockwise
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
