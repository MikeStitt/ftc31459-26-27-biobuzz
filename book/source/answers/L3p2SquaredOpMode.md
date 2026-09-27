# L3p2SquaredOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L3p2SquaredOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: deadband each raw stick into its own variable, the way L3p1
        //         did, then square each of those into a second variable, then hand
        //         the two to drivetrain.sticks(). One step to a line: no call
        //         inside another call.
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftDeadbanded = deadband(leftRawSpeed, DEADBAND);
        double rightDeadbanded = deadband(rightRawSpeed, DEADBAND);

        double leftSpeed = squared(leftDeadbanded);
        double rightSpeed = squared(rightDeadbanded);
        drivetrain.sticks(leftSpeed, rightSpeed);
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: the value times itself, with the sign it started with. Squaring
        //         a negative number the ordinary way loses the minus sign, which
        //         would drive the robot forwards when the driver asked for
        //         backwards, so put it back.
        //         Works when: LessonsTest.l3p2_halfAStickIsAQuarterOfThePower
        //         passes.
        return value;
```

What the solutions line has there:

```java
        double magnitude = value * value;
        if (value < 0.0) {
            return -magnitude;
        } else {
            return magnitude;
        }
```
