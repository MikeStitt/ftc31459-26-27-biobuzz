# Panels, Sloth and Ivy

How to use the dashboard and hot reload on this robot.

## What each piece does

| Thing | What it is | Where you see it |
|---|---|---|
| **Panels** | Web dashboard: telemetry, live graphs, field view, config | `192.168.43.1:8001` |
| **Sloth** | Hot reload. Pushes only TeamCode, in about a second | `deploySloth` Gradle task |
| **Ivy** | Pedro's command scheduler. Sequences autonomous steps | In code |
| **AutoTune** | Pedro's tuning pages (already installed) | `192.168.43.1:10158` |

Panels and AutoTune are different servers on different ports. Both run inside
the Robot Controller app. Neither needs starting from code.

## One-time setup

### 1. Gradle version

This repo is pinned to Gradle **8.14.3**, not the 9.1.0 the FTC SDK ships.
Sloth's plugin calls an API that Gradle 9 removed, and on Gradle 9 the module
**cannot install at all** — while `assembleDebug` still passes, so it looks
like something else is wrong. If Android Studio offers to upgrade Gradle,
**say no.**

### 2. Android Studio

Any version from 2025.1.2 onward works. Quail 4 (2026.1.4) is fine — it accepts
AGP 7.1 through 9.4 and this project is on 8.13.2.

**Decline the AGP upgrade prompt.** Upgrading AGP to 9.x would force Gradle 9,
which breaks Sloth. AGP 8.13.2 + Gradle 8.14.3 is the supported pair here.

Set **Gradle JDK to 17** under Settings → Build Tools → Gradle. This is a
per-laptop setting and cannot be committed.

### 3. Sloth run configurations

Two one-time additions in Android Studio.

**Deploy configuration:**
1. Run → Edit Configurations → **+** → Gradle
2. Gradle project: type `:TeamCode`
3. Tasks: `deploySloth`
4. Name it "Deploy Sloth" and save

Studio will not autocomplete these task names. Type them anyway.

**Fix the normal install:**
1. Edit the existing TeamCode run configuration
2. Add a "Run Gradle task" step with task `removeSlothRemote`
3. Drag it **first**, before the install step, and save

This clears the staged hot-reload jar before a full install, so the two
deployment paths do not fight each other.

## Day-to-day

- **Changed an OpMode?** Run **Deploy Sloth**. Under a second.
- **Changed a dependency, or a file outside `org.firstinspires.ftc.teamcode`?**
  Do a full install. Sloth only reloads classes in that package tree.
- Sloth applies the swap when the current OpMode ends, so deploying while
  something is running is safe.

### If the robot stops accepting code

A stale Sloth payload keeps replacing what you upload. Common this season
because Pedro 3's `tuning` artifact pulls Sloth in transitively. With adb
connected:

```sh
adb shell rm -rf /storage/emulated/0/FIRST/dairy/sloth/*
```

Or delete that folder via Studio's Device File Explorer.

## Using Panels

Join the robot's Wi-Fi, open `http://192.168.43.1:8001`.

Run **TeleOp + Panels** or **Auto: Drive 24in** and you get:

**Telemetry panel** — one line per value:
```
loop/count: 412
loop/period_ms: 4.83
loop/max_period_ms: 21.4
pose/x_in: 6.42
vel/speed_ips: 18.4
...
```

**Graph panel** — every one of those lines is also a plottable series. Pick
them from the list:

| Series | Meaning |
|---|---|
| `loop/count` | Loops that have finished. 0 inside the first loop, 1 once it ends |
| `loop/period_ms` | Milliseconds for the last loop |
| `loop/max_period_ms` | Slowest loop since start() — catches spikes sampling misses |
| `loop/rate_hz` | Loop rate |
| `loop/uptime_s` | Seconds since start() |
| `pose/x_in`, `pose/y_in` | Position, inches |
| `pose/heading_deg` | Heading, degrees |
| `vel/vx_ips`, `vel/vy_ips` | World-frame velocity |
| `vel/speed_ips` | Overall speed |
| `vel/omega_radps` | Angular velocity |
| `vel/forward_ips`, `vel/strafe_ips` | Body-frame velocity |
| `vel/tangential_ips` | Speed along the current path. `NaN` when there is no path — i.e. always in teleop |

A series only shows up in the picker **after it has been sent once**, so start
the OpMode before hunting for it. The picker is a flat list in the order
series were first sent; the `loop/` `pose/` `vel/` prefixes are just for
readability.

**The Driver Station shows nothing unless an OpMode asks it to.** Space there
is scarce, so `Tracker` prints only what it is told to print.
`Tracker.printPoseSpeedLoopToDs(follower)` is the three-line summary — pose,
speed, loop — and both example OpModes call it. The full set goes to Panels.

**Graphs are sampled, not per-loop.** The robot sends to Panels every 75 ms
by default and drops the loops in between. With a ~5 ms loop, about 1 loop
in 15 reaches the graph.

### Reading the graphs

- `loop/period_ms` should sit low and flat. Because of sampling, a single slow
  loop usually won't show here — watch `loop/max_period_ms` instead. A step up
  in `loop/max_period_ms` means some loop blocked: usually a slow sensor read or
  a loop printing too much.
- `pose/x_in` during the auto should ramp smoothly to 24 and stop.
- `vel/tangential_ips` should rise, plateau, then decay to zero. A long tail
  near zero means the follower is still correcting at the end of the path.
- `vel/strafe_ips` should stay near zero on a straight drive. If it doesn't,
  the odometry pod offsets in `Constants.java` are probably off.

### Field view

Open the **Field** panel. Nothing to configure in the Panels UI — the code
sends the Pedro coordinate preset with every frame. Four things are drawn:

| Drawn as | What it is | When |
|---|---|---|
| Blue 18×18 square + line | The robot, where odometry says it is; the line shows which way it faces | Always |
| Green square + line | The **aim point**: the nearest point on the path, at the heading the path wants there | Following a path, or holding a pose |
| Orange line | The path segment being followed | Following a path |
| Grey line | The trail — where the robot has been since the OpMode started | Once it has moved an inch |

**The gap between the blue and green squares is the tracking error.** The
green square is the *nearest* point on the path, not a time-based target, so
it sits beside the robot, never ahead or behind — it shows how far off the line
and how crooked the robot is, not whether it's running late.

In teleop there's no path, so you only see the robot and its trail.

This replaces Pedro 2's `Drawing.drawDebug(follower)`. That method lived in the
Pedro 2 quickstart and does not exist in Pedro 3; code or advice that calls it
is written for Pedro 2.

**It will start in a corner.** Pedro puts `(0, 0)` in a field corner, with the
field spanning 0–144 inches. Both OpModes start the robot at `(0, 0)`, so it
draws in the corner with most of the square off the field. That's expected.

To start the autonomous mid-field, change these in `DriveForward24OpMode`:

```java
private final Pose start = poses.of(72, 72, 0);
private final Pose end   = poses.of(72 + DISTANCE_IN, 72, 0);
```

and the "Remaining" line to:

```java
Tracker.printToDs("Remaining  %.1f in", end.x() - follower.pose().x());
```

The robot still physically drives 24 inches — only the reported numbers
change. `pose/x_in` will then graph 72 → 96 instead of 0 → 24.

For the teleop, add this in `init()` after `Constants.create`:

```java
follower.setPose(PoseFactory.degrees().of(72, 72, 0));
```

with `import com.pedropathing.api.PoseFactory;`.

## Adding your own logging

Panels 1.0 has **no `graph()` method**. The Graph panel parses telemetry
text: any line containing `name: number` becomes a series. `addData` writes
exactly that format, so it is both the text call and the graph call.

**Publish through `Tracker`.** Every method on it is static, so an OpMode, an
arm, a flywheel or a localizer all log the same way:

```java
import org.firstinspires.ftc.teamcode.base.Tracker;

Tracker.publish("arm/target", target);                       // Panels AND the .wpilog
Tracker.publish("arm/position", armMotor.getCurrentPosition());
Tracker.publish("arm/holding", holding);                     // boolean
Tracker.publish("arm/target_pose", pose);                    // x_in, y_in, heading_deg
Tracker.flightlog.recordOutput("arm/samples", raw);          // the file only
Tracker.printToDs("Arm ready");                              // the driver's screen
```

Names can contain anything except a colon. `Tracker.enablePanels(false)` stops
anything reaching Panels; publishing still writes the file and `printToDs`
still reaches the driver.

`Tracker.endLoop(follower)` flushes Panels and the Driver Station once per loop,
from `loopAfter()`. Do not call `telemetry.update()` or `panels.update()`
yourself — a second flush wipes what the first one sent, and a test fails the
build if a bare `telemetry` appears anywhere outside `Tracker`.

To sample faster than every 75 ms, call `panels.setUpdateInterval(20);` in
`start()`. It costs more Wi-Fi traffic.

## The autonomous

`DriveForward24OpMode` drives 24 inches forward holding heading, built with Ivy:

```java
private Command routine() {
    return sequential(
            follow(follower, driveForward())
    );
}
```

To add a step, add a line. Each runs after the previous finishes:

```java
return sequential(
        follow(follower, driveForward()),
        follow(follower, turnAndScore())
);
```

Two things that will bite you if changed:

- **`Scheduler.reset()` in `init()`.** Ivy's scheduler is static and survives
  between runs. Without the reset, a second run replays leftover commands.
- **`.constant()` not `.linear()`.** Pedro issues #176 and #181 both report
  `.linear()` interpolating heading backwards on line paths in 3.0.x. This path
  holds heading, so constant is correct anyway.


# Versions

| Component | Version |
|---|---|
| FTC SDK | 12.0.0 |
| Gradle | 8.14.3 (pinned; SDK ships 9.1.0) |
| AGP | 8.13.2 |
| Pedro Pathing | revhub 3.0.0, tuning 1.0.0 |
| Ivy | core 1.1.1, pedro 1.1.1 |
| Sloth / Load | 0.3.2 |
| Panels | 1.0.13 (via `com.bylazar.sloth:fullpanels:0.3.2+1.0.13`) |
| compileSdk | 34 (overridden in TeamCode) |

Sloth, Load and the Panels version prefix must move together. If you bump one,
bump all three to the matching numbers.
