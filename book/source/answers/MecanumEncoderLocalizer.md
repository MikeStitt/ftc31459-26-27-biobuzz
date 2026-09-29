# MecanumEncoderLocalizer

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/MecanumEncoderLocalizer.java`

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java
import com.pedropathing.math.Twist;
import com.pedropathing.api.PoseFactory;
```

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java
import org.firstinspires.ftc.teamcode.base.HeadingHold;
```

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java
    private static final PoseFactory POSES = PoseFactory.radians();

```

## An unmarked difference

What the lesson leaves blank:

```java
(nothing)
```

What the solutions line has there:

```java

    private double x;
    private double y;
    private double headingOffset;
    private double[] lastWheels;
    private double lastRawHeading;
    private long lastNanos;
    private MotionState state = MotionState.zero();
```

## TODO 1 (L8)

What the lesson leaves blank:

```java
        // TODO 1 (L8): read source.wheelInches() and source.headingRadians().
        // TODO 2 (L8): on the first loop there is nothing to compare against --
        //         remember the values and return.
        // TODO 3 (L8): work out how far each wheel moved since last time, then
        //           forward = (fl + fr + bl + br) / 4
        //           left    = (-fl + fr + bl - br) / 4
        //         (Check that second one against a robot: to strafe LEFT, the
        //         front-left wheel rolls backwards.)
        // TODO 4 (L8): rotate (forward, left) by the heading and add it to x and y:
        //           x += forward * cos(h) - left * sin(h)
        //           y += forward * sin(h) + left * cos(h)
        // TODO 5 (L8): publish the new pose with MotionState.ofTwist(...).
```

What the solutions line has there:

```java
        double[] wheels = source.wheelInches();
        double rawHeading = source.headingRadians();
        long now = clockNanos.getAsLong();

        if (lastWheels == null) {                       // first loop: nothing to compare with
            lastWheels = wheels.clone();
            lastRawHeading = rawHeading;
            lastNanos = now;
            publish(0, 0, 0);
            return;
        }

        double dFrontLeft = wheels[0] - lastWheels[0];
        double dFrontRight = wheels[1] - lastWheels[1];
        double dBackLeft = wheels[2] - lastWheels[2];
        double dBackRight = wheels[3] - lastWheels[3];

        double forwardIn = (dFrontLeft + dFrontRight + dBackLeft + dBackRight) / 4;
        double strafeLeftIn = (-dFrontLeft + dFrontRight + dBackLeft - dBackRight) / 4;

        double dHeading = HeadingHold.wrap(rawHeading - lastRawHeading);
        double midHeading = lastRawHeading + headingOffset + dHeading / 2;   // halfway through the step

        x += forwardIn * Math.cos(midHeading) - strafeLeftIn * Math.sin(midHeading);
        y += forwardIn * Math.sin(midHeading) + strafeLeftIn * Math.cos(midHeading);

        double seconds = (now - lastNanos) / 1e9;
        lastWheels = wheels.clone();
        lastRawHeading = rawHeading;
        lastNanos = now;

        if (seconds > 0) {
            publish(forwardIn / seconds, strafeLeftIn / seconds, dHeading / seconds);
        } else {
            publish(0, 0, 0);
        }
    }

    private void publish(double forwardSpeedInPerS, double strafeLeftSpeedInPerS,
                         double turnCcwSpeedRadPerS) {
        state = MotionState.ofTwist(
                POSES.of(x, y, lastRawHeading + headingOffset),
                new Twist(forwardSpeedInPerS, strafeLeftSpeedInPerS, turnCcwSpeedRadPerS));
```

## TODO 6 (L8)

What the lesson leaves blank:

```java
        // TODO 6 (L8): start counting from this pose. The IMU's heading can't be
        //         moved, so remember the DIFFERENCE and add it from now on.
```

What the solutions line has there:

```java
        x = pose.x();
        y = pose.y();
        headingOffset = pose.heading() - lastRawHeading;
        publish(0, 0, 0);
```

## TODO 5 (L8)

What the lesson leaves blank:

```java
        return MotionState.zero();   // TODO 5 (L8): return the pose you worked out
```

What the solutions line has there:

```java
        return state;
```

## TODO 7 (L8)

What the lesson leaves blank:

```java
        // TODO 7 (L8): back to zero.
```

What the solutions line has there:

```java
        x = 0;
        y = 0;
        headingOffset = -lastRawHeading;
        lastWheels = null;
        publish(0, 0, 0);
```

The two lines also differ in 2 run(s) of comment lines, which are not
blanks and are not shown.
