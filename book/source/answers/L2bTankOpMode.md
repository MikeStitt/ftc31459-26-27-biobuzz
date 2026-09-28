# L2bTankOpMode

The blanks in this file, filled in from `e74ab6c`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L2bTankOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: read both sticks' y axes into named doubles, negating each one
        //         the way L2a did, and hand them to the drivetrain:
        //         drivetrain.sticks(leftSpeed, rightSpeed);
```

What the solutions line has there:

```java
        double leftSpeed = -gamepad1.left_stick_y;
        double rightSpeed = -gamepad1.right_stick_y;
        drivetrain.sticks(leftSpeed, rightSpeed);
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: log the four stick axes, the A button and the pressed and
        //         released events, the same as L2a. The loop count and the time
        //         are gone: the robot logs both for itself.
        //         Works when: LessonsTest.l2b_theSticksDriveTheWheelsLikeATank
        //         and LessonsTest.l2b_theSticksMoveTheSimulatedRobot pass.
```

What the solutions line has there:

```java
        Tracker.publish("stick/leftY", leftSpeed);
        Tracker.publish("stick/leftX", gamepad1.left_stick_x);
        Tracker.publish("stick/rightY", rightSpeed);
        Tracker.publish("stick/rightX", gamepad1.right_stick_x);

        boolean buttonA = gamepad1.a;
        Tracker.publish("driver pressed A", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("lesson/event", "button A pressed");
            } else {
                Tracker.publish("lesson/event", "button A released");
            }
        }
        previousButtonA = buttonA;
```
