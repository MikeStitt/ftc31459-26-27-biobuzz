# LessonsDriveTrain

The blanks in this file, filled in from `7475a49`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/LessonsDriveTrain.java`

## TODO 1 (L2p2)

What the lesson leaves blank:

```java
        // TODO 1 (L2p2): put each power into its own slot of wheelPowers, using
        //         FL, FR, BL and BR to say which slot is which. One line each.
        //         wheelPowers[FL] = frontLeftPower;  and so on.
        //         Then call normalized(wheelPowers), which is L4's job and does
        //         nothing yet, and writeWheels(), which your drivetrain wrote.
        //         Works when: the wheels turn the way the sticks say, and
        //         LessonsTest.l2p2_theSticksDriveTheWheelsLikeATank passes.
```

What the solutions line has there:

```java
        wheelPowers[FL] = frontLeftPower;
        wheelPowers[FR] = frontRightPower;
        wheelPowers[BL] = backLeftPower;
        wheelPowers[BR] = backRightPower;

        normalized(wheelPowers);
        writeWheels();
```

## TODO 2 (L4)

What the lesson leaves blank:

```java
        // TODO 2 (L4): find the biggest of the four, ignoring minus signs, or 1 if
        //         none of them reaches 1. Math.abs takes the minus sign off, and
        //         Math.max picks the bigger of two: give the magnitude its own
        //         variable. Then divide every power by that number, in a second
        //         loop, and hand the array back.
        //         Handing them back untouched is what happens now, which is why
        //         L2p2 drives fine and L4 pulls to one side at full turn.
        //         Works when: LessonsTest.l4_arcadeUsesOneStickToDriveAndOneToTurn
        //         passes, and the robot drives straight with the drive stick and
        //         the turn stick both all the way forward.
```

What the solutions line has there:

```java
        double max = 1.0;
        for (double power : powers) {
            double magnitude = Math.abs(power);
            max = Math.max(max, magnitude);
        }
        for (int wheel = 0; wheel < powers.length; wheel++) {
            powers[wheel] /= max;
        }
```

## TODO 3 (L6)

What the lesson leaves blank:

```java
        // TODO 3 (L6): make a new four-slot array in commandedWheels and put each
        //         power in its own slot, the same way driveWheelsNow does.
```

What the solutions line has there:

```java
        commandedWheels = new double[4];
        commandedWheels[FL] = frontLeftPower;
        commandedWheels[FR] = frontRightPower;
        commandedWheels[BL] = backLeftPower;
        commandedWheels[BR] = backRightPower;
```

## TODO 4 (L6)

What the lesson leaves blank:

```java
        // TODO 4 (L6): forget the four powers, so drive() goes back to asking mix().
        //         Setting commandedWheels to null is how a field says "nothing
        //         here".
```

What the solutions line has there:

```java
        commandedWheels = null;
```

## TODO 5 (L6)

What the lesson leaves blank:

```java
        // TODO 5 (L6): work out the four powers and send them on. If a lesson has
        //         not commanded the wheels, ask mix() for them and hand what comes
        //         back to normalized(), each into its own variable, then
        //         copyInto(wheelPowers, ...). If a lesson has commanded them,
        //         copyInto(wheelPowers, commandedWheels) instead. An if and an
        //         else, not a ?. Then writeWheels().
        //         Works when: L6FollowerDriveTrainTest passes, the robot drives on
        //         the sticks in L6, and L9 drives its 24 inches.
```

What the solutions line has there:

```java
        if (commandedWheels == null) {
            double[] mixed = mix(powers);
            double[] scaled = normalized(mixed);
            copyInto(wheelPowers, scaled);
        } else {
            copyInto(wheelPowers, commandedWheels);
        }

        writeWheels();
```
