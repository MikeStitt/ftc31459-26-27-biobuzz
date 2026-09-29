# L10ForwardThenStrafeOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L10ForwardThenStrafeOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: drive start -> corner, holding the heading it starts at.
        //         That is forward, because the robot faces +y.
        // TODO 2: then corner -> end, holding that same heading. The robot does
        //         not turn, so this leg is a sideways strafe.
        // TODO 3: finish with hold(follower, end) so it stays put.
        //         .constant(pose) takes the heading from a pose. The number
        //         form is radians, so .constant(90) is not 90 degrees.
        return Command.NOOP;
```

What the solutions line has there:

```java
        return sequential(
                follow(follower, line(start, corner).constant(start)),
                follow(follower, line(corner, end).constant(start)),
                hold(follower, end)
        );
```
