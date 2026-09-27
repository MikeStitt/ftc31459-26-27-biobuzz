# L8CompareLocalizersOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L8CompareLocalizersOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: run your localizer alongside the real one, named "encoders":
        //         shadow.add("encoders",
        //                 new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

What the solutions line has there:

```java
        shadow.add("encoders", new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: holonomic driving, same as lesson 5 -- but through the
        //         follower now, the way lesson 6 handed the drivetrain over.
```

What the solutions line has there:

```java
        Drive.holonomic(follower,
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
