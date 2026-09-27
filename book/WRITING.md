# How the guide is written

**Status: controlling.** Written 2026-09-27.

The register and the voice belong to
[`prose-style.md`](../../.claude/rules/parts/prose-style.md) and nothing here repeats it. What is
here is what that file does not say: the shape of a task page, how words are introduced, what a
picture has to be, and what a simplification may do.

## Every task page has the same five parts

- **What you will have when this is done.** One or two sentences, about the robot and not about the
  code. A student deciding what to do next reads only this.
- **Before you start.** The tasks this one needs finished, and the state the robot has to be in: a
  battery in, a particular OpMode already working, a number already measured. A student who missed a
  session starts here, so this part is what makes the guide self-paced.
- **The steps.** One thing to do per step, each carrying **you'll know it worked when** and **if it
  didn't**.
- **What you just did.** One paragraph of theory, written after the robot has already done the
  thing.
- **Where next.** The task or tasks this one unlocks, so the guide can be walked without the
  contents page.

## Write the check from a failure somebody saw

An **if it didn't** line names the symptom a student will actually see. A guessed symptom sends them
looking for something that never appears, which is worse than saying nothing. Where the failure has
not been reproduced, the line says it comes from reading the code rather than from a robot.

## Put the explanation in the step that needs it

A term is introduced where a student first has to use it, named, and reused after that. No page of
theory ahead of the task that needs it. `task.cheatsheet` collects every term with the task that
introduced it.

## A simplification is allowed, and is marked

"The motor goes twice as fast at twice the power" earns its place in L2. The rule is that the guide
never says something a student will later have to unlearn silently: where a simplification gets
corrected later, the page says so in one line and names the task that corrects it.

## A picture is registered before it is drawn

Every figure has a row in [`FIGURES.md`](FIGURES.md) and an id of the form `fig-<something>`. A
figure that does not exist yet is a pencilled box in the page, so the gap is visible to a reader and
not blocking to a writer. `tools/check_figures.py` checks both directions.

## Running the gate

```
python3 -m venv book/.venv
book/.venv/bin/pip install -r book/requirements.txt
cd book && PATH="$PWD/.venv/bin:$PATH" ninja book
```

`ninja -k 0 book` runs every check even when the first fails, which is what you want when fixing
rather than gating. What each check does is in its own docstring.

## Seeing what a blank asks a student to write

```
cd book && PATH="$PWD/.venv/bin:$PATH" ninja review
```

`ninja review` writes a separate Sphinx site under `review/` and prints where the HTML landed. One
page per lesson file, one section per blank, and each section is the diff that filling that blank
makes — starting from the file as a student sees it and applying one blank at a time, so the context
carries the blanks already done.

It is a view for reading, not a gate and not part of the guide: `ninja book` does not run it, and
its pages are not committed. The answer pages under `source/answers/` say what fills each blank;
this says how much work each one is.
