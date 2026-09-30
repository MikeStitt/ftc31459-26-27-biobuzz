# L3aDeadbandOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L3aDeadbandOpMode.java`

## TODO 1 (L3a)

What the lesson leaves blank:

```java
    // TODO 1 (L3a): pick the number. A stick let go reads a few hundredths, so
    //         0.05 is a good first guess; 0 leaves the creep exactly where it was.
    private static final double DEADBAND = 0;
```

What the solutions line has there:

```java
    private static final double DEADBAND = 0.05;
```

## TODO 2 (L3a)

What the lesson leaves blank:

```java
        // TODO 2 (L3a): put each raw stick through drivetrain.deadband(), with
        //         DEADBAND as the band both times, and hand the two to
        //         drivetrain.sticks().
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftSpeed = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightSpeed = drivetrain.deadband(rightRawSpeed, DEADBAND);
        drivetrain.sticks(leftSpeed, rightSpeed);
```
