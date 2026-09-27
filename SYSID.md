# SysId on an FTC robot

A plan to collect data the WPILib SysId analyzer can read, so the tool works
out kS, kV, kA and the feedback gains for our motors instead of us guessing.

## What SysId actually needs

Read from WPILib's own source (`SysIdRoutineLog.java`) and from the analyzer
(`sysid/src/main/native/cpp/view/DataSelector.cpp`), both v2026.2.2:

**A string channel naming the test.** WPILib writes it as
`sysid-test-state-<name>`, but the analyzer does not care about the channel
name: it asks the user to pick the channel. What it does care about is the
**values**, which it splits on the last `-`:

| Value | Meaning |
|---|---|
| `quasistatic-forward` | slow voltage ramp, forwards |
| `quasistatic-reverse` | slow voltage ramp, backwards |
| `dynamic-forward` | voltage step, forwards |
| `dynamic-reverse` | voltage step, backwards |
| `none` | between tests; data here is ignored |

Anything whose first part is not `quasistatic` or `dynamic`, or whose direction
is not `forward` or `reverse`, is discarded with a warning.

**Three double channels per motor**: voltage, position, velocity. Again the
names are the user's to pick in the tool; WPILib's convention is
`voltage-<motor>-<name>`.

**Units are chosen in the analyzer**, from a list that includes Meters, Feet and
**Inches** -- so we can log inches and inches/second, the units the rest of this
code already uses, and pick Inches in the tool.

This is good news: our WPILOG files are already in the format the tool loads, and
nothing about the library needs to change.

## What FTC does not have

**Voltage control.** An FTC motor takes power, -1 to 1. SysId's whole model is
volts in, motion out.

The way round it is the one every FRC brushed-motor setup uses implicitly:
applied volts = commanded power times battery volts, read from the Control Hub's
own voltage sensor each loop. Battery sag then shows up as what it is -- the
same power giving fewer volts -- rather than as noise in the fit.

That makes the numbers SysId returns genuinely volt-based, so kV comes out in
volts per inch per second, and dividing by a nominal 12 V gives the
power-per-inch-per-second constant that `Constants.powerPerInchPerSecond`
holds today.

## The tests to run

Each is one OpMode run, ending when the driver releases the trigger or the robot
runs out of room.

| Test | What it does | Why |
|---|---|---|
| quasistatic forward | ramps voltage at ~0.25 V/s | slow enough that acceleration is negligible, so it separates kS and kV |
| quasistatic reverse | the same, backwards | friction is not always symmetric |
| dynamic forward | steps straight to ~4 V | acceleration dominates, giving kA |
| dynamic reverse | the same, backwards | |

Roughly 3 to 4 metres of clear floor. The quasistatic runs end when the robot
runs out of space; the dynamic runs take about a second.

## What to build

**`SysIdRecorder`** (base) -- writes the state channel and the per-motor
channels into the flight log, and computes applied voltage from power and
battery voltage. No hardware of its own.

**`SysIdDriveOpMode`** (new `sysid` package) -- one OpMode. The driver picks a test
with the D-pad, holds a trigger to run it, releases to stop. The state channel
goes to `none` between tests, so one file can hold all four runs, which is what
the analyzer expects.

**`RobotHardware` gains the voltage sensor**, so nothing else looks up hardware.

## Using the result

1. Run all four tests in one OpMode session.
2. Download the log from `192.168.43.1:8080/corbelsflightlog`.
3. Open SysId, load the file, pick the state channel and one motor's three
   channels, set units to Inches.
4. Read kS, kV, kA, and the feedback gains it suggests.
5. `kV` in volts per inch per second becomes our power-per-inch-per-second by
   dividing by 12; kS is the power needed just to start moving, which lesson 16
   currently ignores.

## What this is not

Not an automatic tuner. SysId gives a model of one motor and a suggested
controller; whether those gains suit the robot is still a judgement. The value
is that the model comes from measurement rather than from a number someone
typed.
