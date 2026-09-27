# L3p1DeadbandOpMode

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L3p1DeadbandOpMode.java`

## TODO 1

What the lesson leaves blank:

```java
        // TODO 1: put each raw stick through deadband(), with DEADBAND as the
        //         band both times, and hand the two to drivetrain.sticks().
        double leftSpeed = 0;
        double rightSpeed = 0;
```

What the solutions line has there:

```java
        double leftSpeed = deadband(leftRawSpeed, DEADBAND);
        double rightSpeed = deadband(rightRawSpeed, DEADBAND);
        drivetrain.sticks(leftSpeed, rightSpeed);
```

## TODO 2

What the lesson leaves blank:

```java
        // TODO 2: if the value is smaller than the band, ignoring its minus sign,
        //         the answer is 0. Otherwise the answer is the value itself. An if
        //         and an else, and Math.abs takes the minus sign off.
        //         Works when: LessonsTest.l3p1_aNearlyCentredStickCountsAsCentred
        //         passes, and the robot sits still with the sticks let go.
        return value;
```

What the solutions line has there:

```java
        if (Math.abs(value) < band) {
            return 0.0;
        } else {
            return value;
        }
```
