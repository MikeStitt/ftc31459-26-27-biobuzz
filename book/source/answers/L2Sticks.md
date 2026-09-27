# L2Sticks

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L2Sticks.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: read both sticks' y axes, negating each one so that pushing
        //         away from the driver is a positive number.
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftSpeed = -gamepad1.left_stick_y;
        double rightSpeed = -gamepad1.right_stick_y;
        tank.sticks(leftSpeed, rightSpeed);
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: hand them to the drivetrain: tank.sticks(leftSpeed, rightSpeed);

        // TODO 3: log all four stick axes, so Panels can draw them:
        //         Tracker.publish("stick/leftY", leftSpeed);
        //         then stick/leftX, stick/rightY and stick/rightX.
```

What the solutions line has there:

```java
        Tracker.publish("stick/leftY", leftSpeed);
        Tracker.publish("stick/leftX", gamepad1.left_stick_x);
        Tracker.publish("stick/rightY", rightSpeed);
        Tracker.publish("stick/rightX", gamepad1.right_stick_x);
```

## TODO 4

What the lesson leaves blank:

```java
        // TODO 4: when gamepad1.a is pressed, send "driver pressed A" to Panels:
        //         buttons.whenPressed(() -> gamepad1.a,
        //                 Commands.instant(() -> Tracker.publish("driver pressed A", true)));
```

What the solutions line has there:

```java
        buttons.whenPressed(() -> gamepad1.a,
                Commands.instant(() -> Tracker.publish("driver pressed A", true)));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
