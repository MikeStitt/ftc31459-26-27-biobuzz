# L12RobotRelativeButton

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L12RobotRelativeButton.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: if gamepad1.right_bumper is held, drive robot relative
        //       (Drive.holonomic); otherwise field relative.
        //       Held, not toggled -- ask a driver why.
        //       Log it too: Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

What the solutions line has there:

```java
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCcwSpeed = -gamepad1.right_stick_x;
        if (gamepad1.right_bumper) {
            Drive.holonomic(follower, forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        } else {
            Drive.fieldRelative(follower, forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        }
        Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
