# Figures, taken and not taken

**Status: controlling.** The register every figure in the guide is listed in.

A figure that does not exist yet is **pencilled**: the page shows a visible box carrying the
figure's id and what the picture must show, so a reader can see the guide is unfinished there and a
writer can keep going. `tools/check_figures.py` checks both directions, and it is the only place the
number of pencilled figures is counted.

How to add one: put the box in the page and the row here, in the same commit.

```
:::{admonition} fig-robot-front
:class: pencil
What the picture must show, in one sentence.
:::
```

| id | What it must show | How it is obtained | Status |
| --- | --- | --- | --- |
| `fig-robot-front` | The robot from the front, on the floor, with both gamepads beside it, so a student can match what is in front of them to what the guide calls each part | Photo of the robot | pencilled |
| `fig-gamepad-sticks` | Gamepad 1 from above, with the left stick, the right stick and the A button labelled, and an arrow showing which way each y axis counts up | Photo of a gamepad | pencilled |
| `fig-wheel-names` | The robot from above with its nose marked, and each of the four wheels labelled front left, front right, back left and back right | Photo of the robot, labelled | pencilled |
| `fig-wheel-forward` | One wheel from the side, with an arrow on the top of the tyre showing which way it travels when that wheel is driving the robot forward | Photo of the robot, labelled | pencilled |
| `fig-stick-shaping` | Three graphs side by side, each with the stick from -1 to 1 along the bottom and the power out from -1 to 1 up the side: the raw stick straight, the deadbanded stick with a flat step at the middle, and the squared stick as an S | A drawing | pencilled |
| `fig-mecanum-x` | The robot from above with its nose marked, and the top roller of each of the four wheels drawn as a line at 45 degrees, so the four lines make an X across the robot | A drawing | pencilled |
| `fig-wheel-handover` | Two arrows reaching the same four wheels, one from the path follower and one from the driver's sticks, with the commanded-wheels switch between them showing which one gets through | A drawing | pencilled |
| `fig-odometry-step` | One loop of dead reckoning: the four wheel travels since the last loop, the forward and left arrows those add up to, the same pair turned to point along the robot's heading, and the old estimate with that pair added on the end | A drawing | pencilled |
