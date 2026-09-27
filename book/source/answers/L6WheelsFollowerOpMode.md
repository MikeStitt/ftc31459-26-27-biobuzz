# L6WheelsFollowerOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L6WheelsFollowerOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
    // TODO 1: L5 made an L5HolonomicDriveTrain. This lesson needs an
    //         L6FollowerDriveTrain instead, here and on the line below that
    //         builds it.
    private L5HolonomicDriveTrain drivetrain;
```

What the solutions line has there:

```java
    private L6FollowerDriveTrain drivetrain;
```

## TODO 2

What the lesson leaves blank:

```java
        drivetrain = new L5HolonomicDriveTrain(hardware);
        // TODO 2: hand the drivetrain to initAfter(), so the follower holds
        //         it. L2 through L5 called initAfter() with nothing, because
        //         they drove their own wheels.
        //         Works when: L6FollowerDriveTrainTest passes and the robot
        //         drives on the sticks.
        initAfter();
```

What the solutions line has there:

```java
        drivetrain = new L6FollowerDriveTrain(hardware);
        initAfter(drivetrain);
```
