# L6FollowerDriveTrain

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L6FollowerDriveTrain.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: wrap the three numbers in a new DrivePowers(...), run them
        //         through mix() and then normalized(), and hand the four powers
        //         that come back to setCommandedWheels(). Going through mix() is
        //         the point: the sticks and the follower then agree about which
        //         wheel does what.
```

What the solutions line has there:

```java
        double[] wheels = normalized(
                mix(new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed)));
        setCommandedWheels(wheels[0], wheels[1], wheels[2], wheels[3]);
```

## TODO 2

What the lesson leaves blank:

```java
                // TODO 2: front left
                0,
                // TODO 3: front right
                0,
                // TODO 4: back left
                0,
                // TODO 5: back right
                0};
```

What the solutions line has there:

```java
                forwardSpeed - strafeLeftSpeed - turnCcwSpeed,      // front left
                forwardSpeed + strafeLeftSpeed + turnCcwSpeed,      // front right
                forwardSpeed + strafeLeftSpeed - turnCcwSpeed,      // back left
                forwardSpeed - strafeLeftSpeed + turnCcwSpeed};     // back right
```

## TODO 6

What the lesson leaves blank:

```java
        // TODO 6: send each of the four powers to its own motor, in the same
        //         order mix() put them in:
        //         frontLeft.setPower(wheels[0]);  and so on for the other three.
```

What the solutions line has there:

```java
        frontLeft.setPower(wheels[0]);
        frontRight.setPower(wheels[1]);
        backLeft.setPower(wheels[2]);
        backRight.setPower(wheels[3]);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
