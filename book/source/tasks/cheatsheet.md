# Cheat sheet

Every table here is read straight out of the lesson code, so it says what the code says.

## What to run

One OpMode name for the Driver Station, one command for the laptop. Each command runs only
that lesson's own tests, which is what makes a red one easy to read. A dash means no test
names that lesson: its check is on the floor.

| Lesson | On the robot | On the laptop |
| --- | --- | --- |
| L2a | `L2a Sticks` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l2a*'` |
| L2b | `L2b Tank` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l2b*'` |
| L3a | `L3a Deadband` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l3a*'` |
| L3b | `L3b Squared` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l3b*'` |
| L4 | `L4 Arcade` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l4*'` |
| L5 | `L5 Holonomic` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l5*'` |
| L6 | `L6 Follower Wheels` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*L6FollowerDriveTrainTest*'` |
| L8 | `L8 Compare Localizers` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l8*'` |
| L9 | `L9 Drive 24` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l9*'` |
| L10 | `L10 Path With Turn` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l10*'` |
| L11 | `L11 Field Relative` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l11*'` |
| L12 | `L12 Robot Relative Button` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l12*'` |
| L13 | `L13 Heading Hold` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l13*'` |
| L14 | `L14 Drive To Pose` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l14*'` |
| L15 | `L15 Combined` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l15*'` |
| L16 | `L16 Velocity Drive` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l16*'` |
| L17a | `L17a Measure ticks per inch` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsDriveTrainTest.theTicksDoorReportsWhatEachEncoderCounted'` |
| L17b | `L17b Measure turn radius` (TeleOp) | -- |

## Who writes what in the shared drivetrain

Every lesson adds to one class, and nothing is written twice. A method written in an early
lesson is the one a later lesson calls.

| Lesson | What it writes in `LessonsDriveTrain` |
| --- | --- |
| L2b | `writeWheels()`, `driveWheelsNow()` |
| L3a and L3b | `deadband()`, `squared()`, `fieldRelative()` |
| L4 | `normalized()` |
| L6 | `mix()`, `setCommandedWheels()`, `releaseCommandedWheels()`, `commandedWheelsAreSet()`, `drive()` |
| L16 | `setCommandedWheelSpeeds()` |
| L17a and L17b | `wheelTicks()` |

## What shows up in Panels

A key ending in `...` has a name added to the end of it, one per wheel or per localizer.

| Log key | Published by |
| --- | --- |
| `Localizer/driveWheelEncoders/...` | `L15CombinedOpMode`, `L8CompareLocalizersOpMode` |
| `command/forward` | `L4ArcadeOpMode`, `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/forward_ips` | `L16VelocityDriveOpMode` |
| `command/left` | `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/left_ips` | `L16VelocityDriveOpMode` |
| `command/turn_ccw` | `L4ArcadeOpMode`, `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/turn_radps` | `L16VelocityDriveOpMode` |
| `drive/aiming` | `L15CombinedOpMode` |
| `drive/mode` | `L14DriveToPoseOpMode`, `L15CombinedOpMode` |
| `drive/robotRelative` | `L12RobotRelativeButtonOpMode` |
| `drive/target_deg` | `L15CombinedOpMode` |
| `driver pressed A` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `heading/deg` | `L13HeadingHoldOpMode` |
| `heading/holding` | `L13HeadingHoldOpMode` |
| `lesson/event` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `lesson/loop_count` | `L2aSticksOpMode` |
| `lesson/seconds_running` | `L2aSticksOpMode` |
| `measure/inches` | `L17aMeasureTicksPerInchOpMode` |
| `measure/radians` | `L17bMeasureTurnRadiusOpMode` |
| `measure/ticks` | `L17aMeasureTicksPerInchOpMode` |
| `measure/ticksPerInch` | `L17aMeasureTicksPerInchOpMode` |
| `measure/turnRadius_in` | `L17bMeasureTurnRadiusOpMode` |
| `measure/wheel_inches` | `L17bMeasureTurnRadiusOpMode` |
| `stick/leftX` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/leftY` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/left_raw` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/left_shaped` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/rightX` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/rightY` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/right_raw` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/right_shaped` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `wheel/...` | `LessonsDriveTrain` |

Generated from the lessons line at `65a2114` and the solutions line at `e74ab6c`.
