# L8CompareLocalizersOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L8CompareLocalizersOpMode.java`

## TODO 1 (L8)

What the lesson leaves blank:

```java
        // TODO 1 (L8): run your localizer alongside the robot's own, named
        //         "driveWheelEncoders" because that is which encoders it reads:
        //         shadowLocalizers.add("driveWheelEncoders",
        //                 new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

What the solutions line has there:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

## TODO 2 (L8)

What the lesson leaves blank:

```java
        // TODO 2 (L8): holonomic driving, same as L5 -- but through the drivetrain
        //         the follower is holding now, the way L6 handed it over. One call
        //         to drivetrain.sticks() with the three sticks.
```

What the solutions line has there:

```java
        drivetrain.sticks(
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
