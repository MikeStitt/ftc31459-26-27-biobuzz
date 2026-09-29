# L9Drive24OpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L9Drive24OpMode.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: return a sequence with one step: follow a straight line from
        //       start to end, holding the heading it starts at.
        //           return sequential(follow(follower, line(start, end).constant(start)));
        //       .constant(pose) takes the heading from a pose. The number form,
        //       .constant(0), is radians, so .constant(90) is not 90 degrees.
        //       Use .constant(), not .linear() -- Pedro 3.0.1 issues #176 and #181.
        return Command.NOOP;
```

What the solutions line has there:

```java
        return sequential(
                follow(follower, line(start, end).constant(start))
        );
```
