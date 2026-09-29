# L14DriveToPoseOpMode

The blanks in this file, filled in from `solutions-sim-gamepad (d7af257)`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L14DriveToPoseOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: when gamepad1.y is pressed, run an instant command that
        //         calls follower.hold(TARGET) and sets drivingItself = true.
        //         (PedroCommands.hold() is instant: it sets the mode and ends.
        //         The follower stays in HOLD until something calls manual().)
```

What the solutions line has there:

```java
        // PedroCommands.hold() is an INSTANT command: it tells the follower to
        // hold the pose and finishes straight away. The follower stays in HOLD
        // until something calls manual() -- so we track that ourselves.
        buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
            follower.hold(TARGET);
            drivingItself = true;
        }));
```

## TODO 2 (L14)

What the lesson leaves blank:

```java
        // TODO 2 (L14): if drivingItself and the sticks are near zero, publish
        //         Tracker.publish("drive/mode", "AUTO"), hand the wheels back with
        //         drivetrain.releaseCommandedWheels(), and return -- the follower
        //         is holding the pose and must not be argued with.
        // TODO 3 (L14): if the driver DOES move a stick, set drivingItself = false,
        //         call follower.manual(0, 0, 0) to leave HOLD -- only manual() can,
        //         and the three zeros are never used -- then publish
        //         Tracker.publish("drive/mode", "DRIVER") and drive field relative.
```

What the solutions line has there:

```java
        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                drivetrain.releaseCommandedWheels();
                return;                       // leave the follower holding
            }
            drivingItself = false;            // the driver takes over
            // Out of HOLD, which only manual() can do. The three zeros are
            // never used -- the sticks command the wheels on the next line.
            follower.manual(0, 0, 0);
        }
        Tracker.publish("drive/mode", "DRIVER");
        drivetrain.fieldRelative(follower.pose().heading(),
                forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
