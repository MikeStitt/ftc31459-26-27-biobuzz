# L15CombinedOpMode

The blanks in this file, filled in from `e74ab6c`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L15CombinedOpMode.java`

## TODO 1 (L15)

What the lesson leaves blank:

```java
        // TODO 1 (L15): add your encoder localizer as "driveWheelEncoders", as in
        //         L8, so the two localizers plot under the same names as they did
        //         there.
```

What the solutions line has there:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
