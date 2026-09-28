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
| `./gradlew :TeamCode:test` | The tests. They drive the sticks in code and assert where the robot ended up |
| `./gradlew :TeamCode:simRun` | Watching. One lesson, running until Ctrl-C, published to AdvantageScope |

## Watching a lesson

Start the simulator:

```
./gradlew :TeamCode:simRun --args="L2bTankOpMode left_stick_y=-1 right_stick_y=-1"
```

The first argument is a lesson's class name without its package; leave it off and you get
`L15CombinedOpMode`. Everything after it sets a field of `gamepad1` by that field's own name, so
`left_stick_y=-1` is the left stick pushed fully forward, and `a=true` is the A button held. The
values are set once and held for the whole run. Driving the sticks while it runs is
`sim.sticks.input` in the workspace's `open-work.md`, and is not built yet.

It prints the port it is listening on and then runs:

```
NT: Listening on NT3 port 1735, NT4 port 5810
L2bTankOpMode running. Connect AdvantageScope to 127.0.0.1 as NetworkTables 4, and Ctrl-C to stop.
```

Point AdvantageScope at `127.0.0.1` and it will find the topics under `sim/`. Add a 2D field and
give it `sim/Pose`. No menu path is written down here, because nobody has opened AdvantageScope
against this yet; that is `nt.watch` in the plan, and this section gets the path once somebody has
done it.

Ctrl-C stops it. It closes the flight log on the way out and prints where it left it.

## What gets published

Everything is under `sim/`.

| Topic | What it is |
|---|---|
| `sim/Pose` | x, y in metres from the centre of the field, and heading in radians. What a field view wants |
| `sim/Mode` | What Pedro's follower says it is doing, as its own name for it |
| `sim/wheels/frontLeft` and the other three | What reached that motor, -1 to 1 |
| `sim/stick/leftY`, `leftX`, `rightY`, `rightX` | What the driver is holding |
| `sim/vel/forward_ips`, `strafe_ips`, `omega_radps` | How fast the robot is going, in its own frame |

None of the struct topics the flight log writes are here: no `Speeds`, no `Twist`, no `Path`, no
`AimPose`. A struct topic has to publish a schema alongside it, and the three `sim/vel` numbers say
what `Twist` would have said. That is `sim.struct.topics` in `open-work.md`.

## Which teleop a test drives

The simulator's own tests drive `SimOpModes.Tank` and `SimOpModes.Driven`, which are built only out
of `base`. That is deliberate: on the lessons branch the lessons are blanks, and a test of the
simulator that drove one would fail there, where a failure outside the `lessons` package is a defect
rather than the point.

The one test that asks a real lesson to move the robot is
`LessonsTest.l2_theSticksMoveTheSimulatedRobot`, and it lives in the `lessons` package, where a
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

The robot starts at Pedro's origin, which is a corner of the field, unless the lesson sets a pose.
So a lesson that drives forward out of `simRun` starts against the wall and heads up the field.

Time is simulated, not measured: `OpModeHarness.clock` advances `stepMs` every loop and nothing
reads the wall clock, so the same number of loops integrates the same motion every run. `simRun`
sleeps `stepMs` between loops as well, which is what makes it look like real time.

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
