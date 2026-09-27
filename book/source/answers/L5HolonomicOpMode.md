# L5HolonomicOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L5HolonomicOpMode.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: three numbers now, not two. left comes from the left stick's x
        //       axis; LEFT is positive and the stick reads positive to the RIGHT,
        //       so that one needs negating too. Log all three.
        double forwardSpeed = 0;
        double strafeLeftSpeed = 0;
        double turnCcwSpeed = 0;
```

What the solutions line has there:

```java
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
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
        Tracker.publish("command/left", strafeLeftSpeed);
        Tracker.publish("command/turn_ccw", turnCcwSpeed);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
