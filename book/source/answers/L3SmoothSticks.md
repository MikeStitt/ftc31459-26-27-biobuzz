# L3SmoothSticks

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L3SmoothSticks.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: shape each stick before it reaches the wheels. Drive.deadband
        //       throws away anything smaller than DEADBAND, and Drive.squared
        //       squares it while keeping its sign. Deadband first, then square.
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftSpeed = Drive.squared(Drive.deadband(leftRawSpeed, DEADBAND));
        double rightSpeed = Drive.squared(Drive.deadband(rightRawSpeed, DEADBAND));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
