# Check your work without a robot

## What you will have when this is done

Two ways to try a lesson with no robot in front of you. One runs every test and tells you which
lessons are finished. The other runs a single lesson on the laptop and draws the robot moving, so
you can see what your code does before it is your turn at the field.

## Before you start

- Android Studio opens the project, and you can type a Gradle command into its terminal. Getting a
  laptop that far has no page yet, so ask a mentor.
- [L2](l2.md) is written, or step 2 has no lesson to show you.
- AdvantageScope installed on the same laptop, for step 2 and step 3.

## The steps

### Step 1: run the tests and read the report

```
./gradlew :TeamCode:test
```

That runs the tests for everything the lessons are built on, and leaves the lesson tests out. It
says so while it runs:

```
Skipping org.firstinspires.ftc.teamcode.lessons.*. Run them with :TeamCode:testLessons.
```

**You'll know it worked when** it ends with `BUILD SUCCESSFUL`. Nothing underneath the lessons is
broken, so whatever fails next is yours.

**If it didn't**, and it says `SDK location not found`, the laptop has no `local.properties` file
yet. That file is not kept in git, and Android Studio writes it the first time it opens the project.
Open the project once, then run the command again.

Now the tests for your own work:

```
./gradlew :TeamCode:testLessons
```

On a fresh copy, where nobody has written a lesson yet, the report counts 66 tests and 54 failures.
Measured on 2026-09-28. Fifty-four failures is not a broken copy. Each one is a lesson nobody has
written, and the count drops as you write them.

**You'll know it worked when** the last lines name a report:

```
> There were failing tests. See the report at: file:///.../index.html
```

Open that link in a browser, click the class and then the test, and you get the message the test
wrote. The console does not give you that. All the console gives you is this:

```
MecanumEncoderLocalizerTest > countsStraightAhead FAILED
    java.lang.AssertionError at MecanumEncoderLocalizerTest.java:63
```

A file and a line, and no word about what went wrong. The report for that same run also holds
`robot relative while held expected:<1.0> but was:<0.0>`, which is L12's blank still open. Every
*if it didn't* line in this guide quotes a message out of the report, so every one of them starts
here.

### Step 2: watch one lesson move

A test says where the robot ended up. It does not show you the robot. For that, run one lesson on
its own:

```
./gradlew :TeamCode:simRun --args="L2bTankOpMode left_stick_y=-1 right_stick_y=-1"
```

The first word inside the quotes is the lesson's class name. Everything after it holds one gamepad
control at one value for the whole run, named the way the gamepad names it. So `left_stick_y=-1` is
the left stick pushed fully forward, and `a=true` is the A button held down.

To push the sticks yourself, plug a gamepad into the laptop and add the word `pad`:

```
./gradlew :TeamCode:simRun --args="L2bTankOpMode pad"
```

One pad becomes `gamepad1` on its own. With two plugged in, hold Start and press A on the one you
want to drive with.

Use `pad`, or set the sticks with arguments, but not both. Together they are an error, and the run
names the setting that clashed.

It prints where it is listening, then runs:

```
NT: Listening on NT3 port 1735, NT4 port 5810
L2bTankOpMode running. Connect AdvantageScope to 127.0.0.1 as NetworkTables 4, and Ctrl-C to stop.
```

Open AdvantageScope and connect to `127.0.0.1` as NetworkTables 4. The topics arrive under `sim/`.
Add a 2D field and give it `sim/Pose`.

**You'll know it worked when** the robot moves. `sim/wheels/frontLeft` and its three neighbours say
what reached each motor, and `sim/vel/forward_ips` says how fast the robot is going.

**If it didn't**, and the robot sits still with all four wheels reading 0, that lesson is still
blank. Watched with L2b's blanks open and both sticks forward: the pose never changed for the whole
run. Nothing is wrong with the simulator. It ran a lesson that writes nothing to the motors.

**If it didn't**, and no topics arrive at all, nothing connected. The address is `127.0.0.1`, and
the kind is NetworkTables 4 rather than a log file.

Ctrl-C stops it, and it prints where it left the flight log.

:::{admonition} fig-advantagescope-sim
:class: pencil
AdvantageScope with a 2D field showing `sim/Pose`, the topic list open beside it, and a graph of
the four `sim/wheels` values, so a student can match their own screen to what this step describes.
:::

:::{admonition} Which way round the field gets drawn is being looked into
:class: note
The corner the arrow starts in and the way it points do not yet agree with the numbers going out.
Trust that the robot moves, and how far it moves, rather than which way it faces on the screen. It
is filed as `sim.field_orientation`.
:::

### Step 3: open the log a failing test left

Some test classes hand you their flight log when they fail. Watched, by asking the simulator's own
test for 400 in when the robot drives 54.75 in:

```
java.lang.AssertionError: drove forward, and got a fair way: 54.75097300967889
AdvantageScope can open what ran:
  /var/folders/.../Tank-20260928-065400.wpilog (37502 bytes)
```

**You'll know it worked when** that file opens in AdvantageScope as a log, not as a connection, and
holds the run up to the moment the test gave up. There is more in it than `simRun` sends out:
`/Robot/Pose`, `/Robot/Speeds`, `/Robot/Twist`, all four wheels, and the loop timing.

**If it didn't**, and the failure names no log, that class never asked for one. The lesson tests are
all like that today. So `l2b_theSticksMoveTheSimulatedRobot` fails with
`drove forward, and got a fair way: 0.0` and no path to open. A log does get written, and it holds 0
bytes, because nothing closed it. Watched. Use step 2 instead, and tell a mentor.

## What you just did

Both halves run the same model of the robot, and it is a small one. Each loop, the four wheel powers
are mixed back into forward, sideways and turn. Each of those three chases what was asked of it with
a 0.15 s lag. The answer is added up into a pose.

| Axis | Flat out |
| --- | --- |
| Forward | 64.4 in/s |
| Sideways | 43.6 in/s |
| Turn | 4.0 rad/s |

So the model has no slip, no scrub, no sagging battery, no floor, no field wall, no motor wired
backwards and no Pinpoint. It will drive straight through the perimeter and never mention it, and it
cannot tell you a path is too fast for the wheels. When the simulator and the robot disagree, the
robot is right.

That is why a green test run is not a finished lesson. These two catch the mistakes that live in the
arithmetic, which are most of them. What is left over is the kind of mistake only a robot on a floor
will show you, and that is why every lesson's last step is a robot on a floor.

## Where next

- [L6](l6.md) hands the wheels to the path follower. From here on you can try each lesson on the
  laptop before you take it to the field.
