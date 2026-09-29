# LessonsDriveTrain

The blanks in this file, filled in from `solutions-sim-gamepad`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/LessonsDriveTrain.java`

## TODO 1 (L2b)

What the lesson leaves blank:

```java
        // TODO 1 (L2b): send each slot of wheelPowers to the motor it belongs to,
        //         naming the slot with FL, FR, BL or BR. One line each:
        //         motors[FL].setPower(wheelPowers[FL]);  and the other three.
        //         Works when: LessonsTest.l2b_theSticksDriveTheWheelsLikeATank
        //         passes. Nothing drives at all until this is written.
```

What the solutions line has there:

```java
        motors[FL].setPower(wheelPowers[FL]);
        motors[FR].setPower(wheelPowers[FR]);
        motors[BL].setPower(wheelPowers[BL]);
        motors[BR].setPower(wheelPowers[BR]);
```

## TODO 2 (L2b)

What the lesson leaves blank:

```java
        // TODO 2 (L2b): put each power into its own slot of wheelPowers, using
        //         FL, FR, BL and BR to say which slot is which. One line each.
        //         wheelPowers[FL] = frontLeftPower;  and so on.
        //         Then call normalized(wheelPowers), which is L4's job and does
        //         nothing yet, and writeWheels(), which you wrote just above.
        //         Works when: the wheels turn the way the sticks say, and
        //         LessonsTest.l2b_theSticksDriveTheWheelsLikeATank passes.
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

## TODO 3 (L4)

What the lesson leaves blank:

```java
        // TODO 3 (L4): find the biggest of the four, ignoring minus signs, or 1 if
        //         none of them reaches 1. Math.abs takes the minus sign off, and
        //         Math.max picks the bigger of two: give the magnitude its own
        //         variable. Put 1 divided by that number into powerScale, so the
        //         log says what the scaling was. Then divide every power by it, in
        //         a second loop with i as the index, and hand the array back.
        //         Handing them back untouched is what happens now, which is why
        //         L2b drives fine and L4 pulls to one side at full turn.
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

        powerScale = 1.0 / max;
        for (int i = 0; i < powers.length; i++) {
            powers[i] = powers[i] / max;
        }
```

## TODO 4 (L6)

What the lesson leaves blank:

```java
        // TODO 4 (L6): make a new four-slot array in commandedWheels and put each
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

## TODO 5 (L6)

What the lesson leaves blank:

```java
        // TODO 5 (L6): forget the four powers, so drive() goes back to asking mix().
        //         Setting commandedWheels to null is how a field says "nothing
        //         here".
```

What the solutions line has there:

```java
        commandedWheels = null;
```

## TODO 6 (L6)

What the lesson leaves blank:

```java
        // TODO 6 (L6): work out where the four powers come from, and send them on.
        //         Declare a double[] sourcePowers with nothing in it. If a lesson
        //         has not commanded the wheels, ask mix() for them into its own
        //         variable, hand that to normalized(), and point sourcePowers at
        //         what comes back. If a lesson has commanded them, point
        //         sourcePowers at commandedWheels instead. An if and an else, not
        //         a ?. Then copy every slot of sourcePowers into wheelPowers in a
        //         loop with i as the index, and call writeWheels().
        //         Works when: L6FollowerDriveTrainTest passes, the robot drives on
        //         the sticks in L6, and L9 drives its 24 inches.
```

What the solutions line has there:

```java
        double[] sourcePowers;
        if (commandedWheels == null) {
            double[] mixed = mix(powers);
            sourcePowers = normalized(mixed);
        } else {
            sourcePowers = commandedWheels;
        }
        for (int i = 0; i < wheelPowers.length; i++) {
            wheelPowers[i] = sourcePowers[i];
        }

        writeWheels();
```

## TODO 7 (L3a)

What the lesson leaves blank:

```java
        // TODO 7 (L3a): if the value is smaller than the band, ignoring its minus
        //         sign, the answer is 0. Otherwise the answer is the value itself.
        //         An if and an else, and Math.abs takes the minus sign off.
        //         Works when: LessonsTest.l3a_aNearlyCentredStickCountsAsCentred
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

## TODO 8 (L3b)

What the lesson leaves blank:

```java
        // TODO 8 (L3b): the value times itself, with the sign it started with.
        //         Squaring a negative number the ordinary way loses the minus
        //         sign, which would drive the robot forwards when the driver asked
        //         for backwards, so put it back.
        //         Works when: LessonsTest.l3b_halfAStickIsAQuarterOfThePower
        //         passes.
        return value;
```

What the solutions line has there:

```java
        double magnitude = value * value;
        if (value < 0.0) {
            return -magnitude;
        } else {
            return magnitude;
        }
```

## TODO 9 (L11)

What the lesson leaves blank:

```java
        // TODO 9 (L11): turn the driver's two field speeds into the robot's own
        //         forward and strafe, by rotating them backwards through the
        //         heading. Take the cosine and the sine of headingRad into their
        //         own variables, then
        //         forwardSpeed    =  fieldXSpeed * cos + fieldYSpeed * sin
        //         strafeLeftSpeed = -fieldXSpeed * sin + fieldYSpeed * cos
        //         Then mix those three, normalize what comes back, and command the
        //         four wheels, which is the same three lines L6's sticks() ends on.
        //         Works when:
        //         LessonsTest.l11_fieldRelativeIgnoresWhichWayTheRobotFaces passes,
        //         and pushing the stick away from the driver moves the robot away
        //         from the driver whichever way it is facing.
```

What the solutions line has there:

```java
        double cos = Math.cos(headingRad);
        double sin = Math.sin(headingRad);
        double forwardSpeed = fieldXSpeed * cos + fieldYSpeed * sin;
        double strafeLeftSpeed = -fieldXSpeed * sin + fieldYSpeed * cos;

        double[] mixed = mix(new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed));
        double[] scaled = normalized(mixed);
        setCommandedWheels(scaled[FL], scaled[FR], scaled[BL], scaled[BR]);
```

## TODO 10 (L16)

What the lesson leaves blank:

```java
        // TODO 10 (L16): put the four speeds asked for into a four-slot array, in
        //         wheel order, and ask measuredSpeeds.all() for the four speeds the
        //         encoders see. Then work out a power for each wheel, in a loop
        //         with i as the index:
        //             feedforward = Constants.powerPerInchPerSecond * wanted[i]
        //             feedback    = VELOCITY_KP * (wanted[i] - measured[i])
        //         and put clampToPower(feedforward + feedback) in that wheel's
        //         slot. Command the four wheels with them, then call
        //         publishWheelSpeeds(wanted, measured, powers) so the log shows
        //         what was asked for, what happened, and the power in between.
        //         Works when:
        //         LessonsTest.l16_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt
        //         passes.
```

What the solutions line has there:

```java
        double[] wanted = new double[4];
        wanted[FL] = frontLeftInPerS;
        wanted[FR] = frontRightInPerS;
        wanted[BL] = backLeftInPerS;
        wanted[BR] = backRightInPerS;

        double[] measured = measuredSpeeds.all();
        double[] powers = new double[4];
        for (int i = 0; i < powers.length; i++) {
            double feedforward = Constants.powerPerInchPerSecond * wanted[i];
            double feedback = VELOCITY_KP * (wanted[i] - measured[i]);
            powers[i] = clampToPower(feedforward + feedback);
        }

        setCommandedWheels(powers[FL], powers[FR], powers[BL], powers[BR]);
        publishWheelSpeeds(wanted, measured, powers);
```

## TODO 11 (L17a)

What the lesson leaves blank:

```java
        // TODO 11 (L17a): fill each slot with what that wheel's encoder has
        //         counted, naming the slot with FL, FR, BL or BR and the motor
        //         through hardware:
        //         ticks[FL] = hardware.frontLeft.getCurrentPosition();  and three
        //         more. Four zeros is what comes back now, so a measurement says
        //         the robot never moved.
        //         Works when:
        //         LessonsDriveTrainTest.theTicksDoorReportsWhatEachEncoderCounted
        //         passes.
```

What the solutions line has there:

```java
        ticks[FL] = hardware.frontLeft.getCurrentPosition();
        ticks[FR] = hardware.frontRight.getCurrentPosition();
        ticks[BL] = hardware.backLeft.getCurrentPosition();
        ticks[BR] = hardware.backRight.getCurrentPosition();
```
