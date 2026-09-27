# L16VelocityDriveOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L16VelocityDriveOpMode.java`

## An unmarked difference

What the lesson leaves blank:

```java
    private WheelVelocities measured;
```

What the solutions line has there:

```java
    private WheelVelocities measuredVelocities;
```

## An unmarked difference

What the lesson leaves blank:

```java
        measured = new WheelVelocities(hardware);
```

What the solutions line has there:

```java
        measuredVelocities = new WheelVelocities(hardware);
```

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: read the sticks as a SPEED, not a power. Full forward asks for
        //         MAX_IPS inches per second; full turn, MAX_TURN_RADPS radians per
        //         second. Use Drive.deadband(value, 0.05) as usual, and remember
        //         the minus signs from lesson 5.
        double forwardSpeedInPerS = 0;
        double strafeLeftSpeedInPerS = 0;
        double turnCcwSpeedRadPerS = 0;
```

What the solutions line has there:

```java
        // 1. The sticks ask for a speed, not a power.
        double forwardSpeedInPerS = Drive.deadband(-gamepad1.left_stick_y, 0.05) * MAX_IPS;
        double strafeLeftSpeedInPerS = Drive.deadband(-gamepad1.left_stick_x, 0.05) * MAX_IPS;
        double turnCcwSpeedRadPerS = Drive.deadband(-gamepad1.right_stick_x, 0.05) * MAX_TURN_RADPS;
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: work out how fast each wheel has to travel for the robot to move
        //         like that. WheelTargets.forMecanum(forward, strafeLeft, turnCcw, radius)
        //         does the arithmetic; the radius is Constants.turnRadiusInches.
        double[] target = new double[4];
```

What the solutions line has there:

```java
        // 2. What each wheel must do for the robot to move like that.
        double[] target = WheelTargets.forMecanum(forwardSpeedInPerS, strafeLeftSpeedInPerS,
                turnCcwSpeedRadPerS, Constants.turnRadiusInches);
```

## TODO 3

What the lesson leaves blank:

```java
        // TODO 3: ask the motors how fast their drivetrain are actually going.
        //         measured.all() hands back all four, in inches per second.
        double[] actual = new double[4];
```

What the solutions line has there:

```java
        // 3. What each wheel is actually doing.
        double[] actual = measuredVelocities.all();
```

## TODO 4

What the lesson leaves blank:

```java
        // TODO 4: guess a power for each wheel, then correct it by the error, and
        //         send all four with drivetrain.setCommandedWheels(...):
        //             power = kV * target + kP * (target - actual)
        //         clamp(...) below keeps the answer inside -1 to 1.
```

What the solutions line has there:

```java
        // 4. Guess the power, then correct it by the error.
```

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java
        for (int i = 0; i < 4; i++) {
            double feedforward = kV * target[i];
            double feedback = kP * (target[i] - actual[i]);
            power[i] = clamp(feedforward + feedback);
        }
        drivetrain.setCommandedWheels(power[0], power[1], power[2], power[3]);
```

The two lines also differ in 3 run(s) of comment lines, which are not
blanks and are not shown.
