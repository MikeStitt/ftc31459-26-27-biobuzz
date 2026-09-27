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
