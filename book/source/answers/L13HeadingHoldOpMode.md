# L13HeadingHoldOpMode

The blanks in this file, filled in from `solutions-sim-gamepad (d7af257)`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L13HeadingHoldOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: build a HeadingHold from the tuned controller:
        //         new HeadingHold(Constants.foresightConfig.headingFeedback.get())
```

What the solutions line has there:

```java
        heading = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
```

## TODO 2 (L13)

What the lesson leaves blank:

```java
        // TODO 2 (L13): ask heading.turn(follower, -gamepad1.right_stick_x) for the
        //         turn power, then drive field relative with it through
        //         drivetrain.fieldRelative(...). Log heading/holding and
        //         heading/deg so Panels can show the hold.
```

What the solutions line has there:

```java
        double turnCcwSpeed = heading.turn(follower, -gamepad1.right_stick_x);
        drivetrain.fieldRelative(follower.pose().heading(),
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, turnCcwSpeed);
        Tracker.publish("heading/holding", heading.target() != null);
        Tracker.publish("heading/deg", Math.toDegrees(follower.pose().heading()));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
