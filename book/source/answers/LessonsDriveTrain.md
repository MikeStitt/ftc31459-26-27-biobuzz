# LessonsDriveTrain

The blanks in this file, filled in from `5e7f23b`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/LessonsDriveTrain.java`

## TODO 

What the lesson leaves blank:

```java
        // TODO: put the four powers into an array in the order front left, front
        //       right, back left, back right, hand it to normalized(), then pass
        //       what comes back to remember() and to writeWheels().
        //       double[] wheels = normalized(new double[]{frontLeftPower, ...});
        //       Works when: the wheels turn the way the sticks say, and
        //       LessonsTest.l2_theSticksDriveTheWheelsLikeATank passes.
```

What the solutions line has there:

```java
        double[] wheels = normalized(new double[]{frontLeftPower, frontRightPower,
                backLeftPower, backRightPower});
        remember(wheels);
        writeWheels(wheels);
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: find the biggest of the four, ignoring minus signs, or 1 if none
        //       of them reaches 1. Then divide every one of them by that number.
        //       Math.abs takes the minus sign off; Math.max picks the bigger of
        //       two. Handing them back untouched is what happens now, which is
        //       why L2 drives fine and L4 pulls to one side at full turn.
        //       Works when: LessonsTest.l4_arcadeUsesOneStickToDriveAndOneToTurn
        //       passes, and the robot drives straight with the drive stick and
        //       the turn stick both all the way forward.
```

What the solutions line has there:

```java
        double max = 1.0;
        for (double power : powers) max = Math.max(max, Math.abs(power));
        if (max == 1.0) return powers;
        for (int i = 0; i < powers.length; i++) powers[i] /= max;
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: remember the four powers in the commandedWheels field, in the
        //       usual order. One line, and it looks like the array in
        //       driveWheelsNow.
```

What the solutions line has there:

```java
        commandedWheels = new double[]{frontLeftPower, frontRightPower,
                backLeftPower, backRightPower};
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: forget the four powers, so drive() goes back to asking mix().
        //       Setting commandedWheels to null is how a field says "nothing
        //       here".
```

What the solutions line has there:

```java
        commandedWheels = null;
```

## TODO 

What the lesson leaves blank:

```java
        // TODO: work out the four powers and send them on. If a lesson has
        //       commanded the wheels, use a copy of those --
        //       commandedWheels.clone() -- and if it has not, use
        //       normalized(mix(powers)). Then hand the four to remember() and to
        //       writeWheels(), the same two calls driveWheelsNow makes.
        //       Works when: L6FollowerDriveTrainTest passes, the robot drives on the
        //       sticks in L6, and L9 drives its 24 inches.
```

What the solutions line has there:

```java
        double[] wheels = commandedWheels != null
                ? commandedWheels.clone()
                : normalized(mix(powers));
        remember(wheels);
        writeWheels(wheels);
```
