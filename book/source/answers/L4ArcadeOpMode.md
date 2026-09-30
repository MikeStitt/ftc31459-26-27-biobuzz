# L4ArcadeOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L4ArcadeOpMode.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: read the two sticks and hand them to drivetrain.sticks(forwardSpeed, turnCcwSpeed).
        //       forward comes from the left stick's y axis, turn from the right
        //       stick's x axis, and BOTH need a minus sign -- the note above says
        //       why the turn one does. Log them as command/forward and
        //       command/turn_ccw so Panels can draw them.
        double forwardSpeed = 0;
        double turnCcwSpeed = 0;
```

What the solutions line has there:

```java
        double forwardSpeed = -gamepad1.left_stick_y;
        double turnCcwSpeed = -gamepad1.right_stick_x;
```

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java

        Tracker.publish("command/forward", forwardSpeed);
        Tracker.publish("command/turn_ccw", turnCcwSpeed);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
