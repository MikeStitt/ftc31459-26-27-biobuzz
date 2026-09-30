# The simulator

How to run a lesson on a laptop and watch the robot move in AdvantageScope.

## What it is

The lessons are real OpModes, and the simulator runs them with fake hardware. `OpModeHarness` gives
an OpMode four fake motors, a fake IMU and a fake battery, and `SimRobot` turns whatever power
reaches those motors into motion. Nothing here runs on the robot: it all lives in `TeamCode`'s test
sources, and NetworkTables is a `testImplementation` dependency, so none of it can reach a Control
Hub.

Two ways to run it:

| Way | What it is for |
|---|---|
| `./gradlew :TeamCode:test` | Everything except the lessons package. They drive the sticks in code and assert where the robot ended up |
| `./gradlew :TeamCode:testLessons` | The lessons package alone. On the lessons line most of it fails, because the blanks are open |
| `./gradlew :TeamCode:simRun` | Watching. One OpMode, running until Ctrl-C, published to AdvantageScope |

## Watching a lesson

Start the simulator:

```
./gradlew :TeamCode:simRun --args="lessons.L2bTankOpMode --left_stick_y=-1 --right_stick_y=-1"
```

The first argument is the OpMode's class name after `org.firstinspires.ftc.teamcode`, so a lesson is
`lessons.L2bTankOpMode` and a simulator teleop is `base.SimOpModes$Tank`. Leaving it off is an
error, not a default. Every option after it is `--name=value` naming one of the 21 controls a
gamepad has, so `--left_stick_y=-1` is the left stick pushed fully forward and `--a=true` is the A
button held. A control is held from before the run starts, so `--a=true` fires `aWasPressed()` on
the first loop and not again.

`--args="--help"` prints all of it, including which field names are refused and why.

To push the sticks yourself instead, plug a gamepad in and use `--pad`. `--pad-check` reports what a
gamepad is doing without running an OpMode, so it can run beside one.

It prints the port it is listening on and then runs:

```
NT: Listening on NT3 port 1735, NT4 port 5810
lessons.L2bTankOpMode running. Connect AdvantageScope to 127.0.0.1 as NetworkTables 4, and Ctrl-C to stop.
```

Point AdvantageScope at `127.0.0.1` and it will find the topics under `sim/`. Add a 2D field and
give it `sim/Pose`, and the robot drives up the field. Watched on 2026-09-28. The menu path for
setting the address is still not written down here: whoever drove those menus did it by hand and the
path was not captured.

Ctrl-C stops it. It closes the flight log on the way out and prints where it left it.

## What gets published

Everything is under `sim/`.

| Topic | What it is |
|---|---|
| `sim/Pose` | A `struct:Pose2d`: x, y in metres from the centre of the field, and heading in radians |
| `sim/Mode` | What Pedro's follower says it is doing, as its own name for it |
| `sim/wheels/frontLeft` and the other three | What reached that motor, -1 to 1 |
| `sim/stick/leftY`, `leftX`, `rightY`, `rightX` | What the driver is holding |
| `sim/vel/forward_ips`, `strafe_ips`, `omega_radps` | How fast the robot is going, in its own frame |

The pose is a struct because AdvantageScope wants one. A bare `double[]` of x, y and heading is what
it calls the legacy numeric array format: it draws that too, warns about it in 2026 and removes it
in 2027. The flight log has always written `struct:Pose2d`, so the topic now carries the same three
little-endian doubles, and the schema that says what they are goes out beside it.

The flight log's other struct topics are still missing: no `Speeds`, no `Twist`, no `Path`, no
`AimPose`. The three `sim/vel` numbers say what `Twist` would have said. That is
`sim.struct.topics` in `open-work.md`.

## Which teleop a test drives

The simulator's own tests drive `SimOpModes.Tank` and `SimOpModes.Driven`, which are built only out
of `base`. `SimOpModes.Edges` and `SimOpModes.Slow` are there for running by hand: one prints a
button and its press edge, the other burns 25 ms in every loop to show a slow OpMode still moves the
robot at field speed. That is deliberate: on the lessons branch the lessons are blanks, and a test
of the simulator that drove one would fail there, where a failure outside the `lessons` package is a
defect rather than the point.

The one test that asks a real lesson to move the robot is
`LessonsTest.l2b_theSticksMoveTheSimulatedRobot`, and it lives in the `lessons` package, where a
blank L2 failing is expected.

## Where a failing test leaves its log

A test that fails names its flight log in the failure message:

```
drove forward, and got a fair way: 54.75097300967889
AdvantageScope can open what ran:
  /var/folders/.../corbelsflightlog-test2706371408438583527/L2bTankOpMode-20260927-032443.wpilog
```

Open that file in AdvantageScope and the run is there up to the moment the assertion went wrong.

A test class gets that by adding one line:

```java
@Rule
public final SimLogs logs = new SimLogs();
```

`SimMotionTest` and `SimPublisherTest` have it. Without the rule a failing test still writes a log,
but it never reaches `stop()`, so the file is closed by nobody and holds 0 bytes.

## What the model does, and what it does not

The whole of the physics is three numbers and a lag. Each loop, the four wheel powers are mixed
back into forward, strafe and turn; each of those chases its commanded speed with a 0.15 s time
constant; and the result is integrated into a pose.

| Axis | Flat out |
|---|---|
| Forward | 64.4 in/s |
| Strafe | 43.6 in/s |
| Turn | 4.0 rad/s |

So the simulator has no slip, no scrub, no battery sag, no floor, no field wall, no motor wired
backwards and no Pinpoint. It cannot tell you a path is too fast for the tyres, and it will happily
drive through the perimeter. **When the simulator and the robot disagree, the robot is right.**

An autonomous starts where its own `startPose()` says. A teleop starts where the last autonomous
finished, which `OpModeStorage.autonomousEndPose` carries between them; with no autonomous before
it, that is the middle of the near wall facing up the field. Nothing starts at Pedro's origin any
more, which is a field corner and outside the perimeter frame.

Time is simulated in a test and measured in a run. `OpModeHarness.loop()` advances `stepMs` and
reads no wall clock, so a test integrates the same motion every time. `simRun` calls `loop(long)`
with the real milliseconds its last pass took, bounded at 100 ms, so the robot moves at the speed it
would move on the field however slow the OpMode's own loop is. Everything periodic runs on a 10 ms
grid measured from the clock read at startup, rather than sleeping a fixed time after each pass.

## Why the NetworkTables version is not the current one

WPILib publishes `ntcore-java` at 2026.2.2, but every `-jni` artifact stops at 2025.3.2, and a
server needs the native library. So both halves are pinned to 2025.3.2 in `TeamCode/build.gradle`,
and both move together when the 2026 natives appear.

Loading that native out of a plain Maven jar takes some care, and `NtNatives` is where it happens.
`RuntimeLoader` and `CombinedRuntimeLoader` both fail, each in its own way, and that class's javadoc
records what they print so nobody tries them again.

## The conversion that is temporary

`base/FieldPose.java` is a copy of `FlightLog.fieldPose` in corbelsflightlog, which converts Pedro's
inches into metres from the centre of the field and applies `fieldQuarterTurns`. The library's copy
is written and unreleased. When it releases, the dependency moves to that version,
`base/FieldPose.java` is deleted, and `SimPublisher` calls the library. `FieldPoseTest` is what
keeps the two agreeing meanwhile.
