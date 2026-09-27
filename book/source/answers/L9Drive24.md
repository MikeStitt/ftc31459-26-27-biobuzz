# L9Drive24

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L9Drive24.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: return a sequence with one step: follow a straight line from
        //       start to end, holding heading 0.
        //           return sequential(follow(follower, line(start, end).constant(0)));
        //       Use .constant(), not .linear() -- Pedro 3.0.1 issues #176 and #181.
        return Command.NOOP;
```

What the solutions line has there:

```java
        return sequential(
                follow(follower, line(start, end).constant(0))
        );
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
