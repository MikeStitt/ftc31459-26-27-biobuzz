# L3bSquaredOpMode

The blanks in this file, filled in from `e74ab6c`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L3bSquaredOpMode.java`

## TODO 1 (L3b)

What the lesson leaves blank:

```java
    // TODO 1 (L3b): pick the number again, the same way L3a did. Each lesson keeps
    //         its own, so changing one never changes the other.
    private static final double DEADBAND = 0;
```

What the solutions line has there:

```java
    private static final double DEADBAND = 0.05;
```

## TODO 2 (L3b)

What the lesson leaves blank:

```java
        // TODO 2 (L3b): deadband each raw stick into its own variable, the way L3a
        //         did, then square each of those into a second variable, then hand
        //         the two to drivetrain.sticks(). One step to a line: no call
        //         inside another call.
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftDeadbanded = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightDeadbanded = drivetrain.deadband(rightRawSpeed, DEADBAND);

        double leftSpeed = drivetrain.squared(leftDeadbanded);
        double rightSpeed = drivetrain.squared(rightDeadbanded);
        drivetrain.sticks(leftSpeed, rightSpeed);
```
