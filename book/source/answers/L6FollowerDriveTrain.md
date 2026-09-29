# L6FollowerDriveTrain

The blanks in this file, filled in from `solutions-sim-gamepad`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L6FollowerDriveTrain.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: wrap the three numbers in a new DrivePowers(...) in its own
        //         variable, hand that to mix() into a second variable, hand that
        //         to normalized() into a third, and pass the four slots to
        //         setCommandedWheels(). One call to a line; nothing nested.
        //         Going through mix() is the point: the sticks and the follower
        //         then agree about which wheel does what.
```

What the solutions line has there:

```java
        DrivePowers drivePowers = new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        double[] mixed = mix(drivePowers);
        double[] scaled = normalized(mixed);
        setCommandedWheels(scaled[FL], scaled[FR], scaled[BL], scaled[BR]);
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: fill each slot in, one line each, naming it with FL, FR, BL or
        //         BR. The four sums are the same ones L5HolonomicDriveTrain uses.
```

What the solutions line has there:

```java
        wheels[FL] = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        wheels[FR] = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        wheels[BL] = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        wheels[BR] = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
```
