# L10PathWithTurnOpMode

The blanks in this file, filled in from `e74ab6c`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L10PathWithTurnOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: drive start -> corner holding heading 0.
        // TODO 2: then corner -> end holding heading 90 degrees, so the robot
        //         turns as it drives the second leg.
        // TODO 3: finish with hold(follower, end) so it stays put.
        return Command.NOOP;
```

What the solutions line has there:

```java
        return sequential(
                follow(follower, line(start, corner).constant(0)),
                follow(follower, line(corner, end).constant(Math.toRadians(90))),
                hold(follower, end)
        );
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
