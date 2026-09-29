# Glossary

Every word here is one the task pages use, and each entry names the page that introduces it. Where
the guide could have invented a friendlier word and did not, the reason is that the code, Pedro's
documentation and the Driver Station all use this one.

**OpMode**
: One program the Driver Station can run. Each lesson is an OpMode, and its name in the list is the
  one in its `@TeleOp` or `@Autonomous` line. [L2](l2.md)

**TeleOp and Autonomous**
: The two kinds of OpMode. A TeleOp reads the gamepads; an autonomous runs on its own. L9 and L10
  are the two autonomous lessons. [L9](l9.md)

**Driver Station**
: The phone or tablet that holds the gamepads and picks the OpMode. Lines printed with
  `Tracker.printToDs` show up there. [L2](l2.md)

**Panels**
: The web page that draws what the code published while it ran. Every `Tracker.publish` key shows
  up there, and [the cheat sheet](cheatsheet.md) lists them. [L3](l3.md)

**Drivetrain**
: The four motors and the code that decides what power each one gets. Every lesson shares one class,
  `LessonsDriveTrain`, and adds to it. [L2](l2.md)

**Deadband**
: A band of stick readings near the middle that count as let go. Without one the robot creeps when
  nobody is touching it. [L3](l3.md)

**Squaring a stick**
: Making a small push mean a smaller power, by multiplying the stick by itself and putting the sign
  back. [L3](l3.md)

**Normalizing**
: Scaling four wheel powers down together when the biggest one is over 1. The robot still goes
  where it was asked. It only goes slower. [L4](l4.md)

**Mixing**
: Turning forward, sideways and turn into four wheel powers with four sums. [L5](l5.md)

**Mecanum**
: A wheel with rollers set at 45 degrees on its rim, which lets the robot drive sideways. The four
  wheels' rollers make an X seen from above. [L5](l5.md)

**Commanded wheels**
: Four powers a lesson has set itself. The path follower cannot overrule them. It gets the wheels
  back when the lesson hands them back. [L6](l6.md)

**Follower**
: Pedro's code that drives the robot along a path, or holds it at a pose. From L6 on it holds the
  drivetrain and the lessons reach past it. [L6](l6.md)

**Pose**
: Where the robot is and which way it faces: x, y and a heading. A pose is a place on the field, not
  a distance from the robot. [L8](l8.md)

**Heading**
: Which way the robot is facing, as an angle. Counter-clockwise counts up, and the code keeps it in
  radians even where a page says degrees. [L8](l8.md)

**Localizer**
: Whatever works out the pose. A robot can run more than one at a time. That is what L8 is for.
  [L8](l8.md)

**Shadow localizer**
: A second localizer that is read and logged but never steers anything. It is how two answers get
  compared on one run. [L8](l8.md)

**Dead reckoning**
: Working out where you are by adding up every step you took, with nothing to check it against. The
  drive encoders do it; the Pinpoint does it better. [L8](l8.md)

**Pinpoint**
: The odometry computer. It has its own two measuring wheels, and it works out the pose. The
  follower believes this one. L17 measures against it. [L8](l8.md)

**Path**
: A line or curve the follower drives along, with a heading to hold while it does.
  [L9](l9.md)

**Hold**
: Telling the follower to sit at one pose and stay there. If something pushes the robot, the
  follower puts it back. [L10](l10.md)

**Field relative**
: Sticks that mean the field. Push away from you and the robot moves away from you, whichever way
  its nose points. [L11](l11.md)

**Robot relative**
: Sticks that mean the robot's own directions. Better for lining up against something.
  [L12](l12.md)

**Heading hold**
: Keeping the nose where the driver left it, and giving it straight back the moment the turn stick
  moves. [L13](l13.md)

**Instant command**
: A command that does its work and finishes in the same breath, rather than running for a while. A
  button press is usually one. [L14](l14.md)

**Feedforward**
: A guess at the power a wanted speed needs, made before anything is measured. Gets most of the way
  there at once. [L16](l16.md)

**Feedback**
: A correction worked out from two speeds: the one asked for and the one measured. It cleans up
  what the guess got wrong. [L16](l16.md)

**Ticks**
: What an encoder counts as a motor turns. A tick is a fixed fraction of a turn, so ticks become
  inches once you know how many make one. [L17](l17.md)

**Ticks per inch**
: How many ticks a wheel counts for an inch of travel. Measured, not looked up. [L17](l17.md)

**Turn radius**
: How far a wheel sits from the middle of the robot, along the diagonal it pushes. It turns radians
  per second into inches per second at the wheel. [L17](l17.md)

**Coast and brake**
: What a motor does at zero power. Braking holds the robot still; coasting lets the wheels roll,
  which is what a measurement you push by hand needs. [L17](l17.md)
