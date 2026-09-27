# L13HeadingHoldTeleOp

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L13HeadingHoldTeleOp.java`

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

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: ask heading.turn(follower, -gamepad1.right_stick_x) for the
        //         turn power, then drive field relative with it. Log
        //         heading/holding and heading/deg so Panels can show the hold.
```

What the solutions line has there:

```java
        double turnCcwSpeed = heading.turn(follower, -gamepad1.right_stick_x);
        Drive.fieldRelative(follower,
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, turnCcwSpeed);
        Tracker.publish("heading/holding", heading.target() != null);
        Tracker.publish("heading/deg", Math.toDegrees(follower.pose().heading()));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
