# L14DriveToPoseOpMode

The blanks in this file, filled in from `7475a49`:

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

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: if drivingItself and the sticks are near zero, call
        //         Tracker.publish("drive/mode", "AUTO") and return without calling
        //         any Drive method -- let the follower hold.
        // TODO 3: if the driver DOES move a stick, set drivingItself = false, call
        //         Tracker.publish("drive/mode", "DRIVER"), and drive field relative.
```

What the solutions line has there:

```java
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCcwSpeed = -gamepad1.right_stick_x;
        boolean driverWantsControl = Math.abs(forwardSpeed) > 0.1
                || Math.abs(strafeLeftSpeed) > 0.1 || Math.abs(turnCcwSpeed) > 0.1;

        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                return;                       // leave the follower holding
            }
            drivingItself = false;            // the driver takes over
        }
        Tracker.publish("drive/mode", "DRIVER");
        Drive.fieldRelative(follower, forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
