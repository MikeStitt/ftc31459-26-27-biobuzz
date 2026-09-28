"""Generate the cheat sheet from the lesson code.

Every table on the page is read out of the two lesson lines at the commits
keep.toml pins, the same commits the answers are generated from, so the page and
the code cannot drift apart: what a student is told to run is what the code says.

The names and the tests come from the lessons line, which is where a javadoc says
`Passes when:` and a blank says `Works when:`. The log keys come from the
solutions line, because a key published inside a blank is not in the lessons line
at all.

Nothing here is written by hand except the headings and the sentences under them.
A name, a test, a method or a log key on the page is there because it is in the
code.

Run it with --check to compare against what is committed instead of writing.
"""

import argparse
import re
import sys
from collections import defaultdict
from pathlib import Path

from answers import git, LESSONS_DIR, pins
from bookpaths import book_root

OUT = "source/tasks/cheatsheet.md"

OPMODE_NAME = re.compile(r'@(TeleOp|Autonomous)\(name\s*=\s*"([^"]*)"')
LESSON_ID = re.compile(r"^L(\d+[ab]?)")
TEST = re.compile(r"\b([A-Z]\w*Test)(?:\.(\w+))?")
TEST_LESSON = re.compile(r"^l(\d+[ab]?)_")
TODO_LESSON = re.compile(r"TODO \d+ \(L(\d+[ab]?)\)")
PUBLISH = re.compile(r'Tracker\.publish\("([^"]*)"')
SHADOW = re.compile(r'shadowLocalizers\.add\("([^"]*)"')
DIVIDER = re.compile(r"^\s*// -+ (.*)$")
SIGNATURE = re.compile(r"^    (?:public|protected)(?: final)? [\w\[\]<>, ]+ (\w+)\(")
WRITES = re.compile(r"^(L\d+[ab]?(?: and L\d+[ab]?)*) writes? (?:this|these)")
NAMES_A_TEST = re.compile(r"(Passes|Works) when:")


def package(commit: str) -> dict[str, list[str]]:
    """Every file in the lessons package at that commit, by file name."""
    listed = git("ls-tree", "-r", "--name-only", commit, LESSONS_DIR + "/").splitlines()
    return {Path(path).name: git("show", f"{commit}:{path}").splitlines()
            for path in listed}


def named_tests(lines: list[str]) -> list[tuple[str, tuple[str, str | None]]]:
    """Every test a file names, each with the lesson its TODO marker was under.

    A `Passes when:` or `Works when:` line runs on over the comment lines after
    it, so each block is read to the end of the comment rather than to the end of
    the line.
    """
    out = []
    todo = None
    collecting = False
    for line in lines:
        marker = TODO_LESSON.search(line)
        if marker:
            todo = marker.group(1)
        start = NAMES_A_TEST.search(line)
        if start:
            collecting = True
            line = line[start.end():]
        elif collecting:
            stripped = line.strip()
            if not (stripped.startswith("*") or stripped.startswith("//")) \
                    or stripped.startswith("*/"):
                collecting = False
                continue
        if collecting:
            for cls, method in TEST.findall(line):
                out.append((todo, (cls, method or None)))
    return out


def whose_test(test: tuple[str, str | None], file_id: str | None,
               todo_id: str | None, rows: dict) -> str | None:
    """The lesson a named test belongs to, by what names it most closely."""
    _, method = test
    if method:
        by_name = TEST_LESSON.match(method)
        if by_name and by_name.group(1) in rows:
            return by_name.group(1)
    if file_id and file_id in rows:
        return file_id
    if todo_id and todo_id in rows:
        return todo_id
    return None


def test_command(tests: list[tuple[str, str | None]], lesson: str) -> str:
    """The narrowest gradle filter that runs exactly the tests named."""
    if not tests:
        return ""
    prefix = f"l{lesson}_"
    one_class = {cls for cls, _ in tests}
    if len(one_class) == 1 and all(m and m.startswith(prefix) for _, m in tests):
        patterns = [f"'*{tests[0][0]}.l{lesson}*'"]
    else:
        patterns = [f"'*{cls}.{method}'" if method else f"'*{cls}*'" for cls, method in tests]
    return "./gradlew :TeamCode:testDebugUnitTest --tests " + " --tests ".join(patterns)


def lessons_table(files: dict[str, list[str]]) -> list[str]:
    """Lesson, what it is called on the robot, and what runs its tests."""
    rows: dict[str, dict] = {}
    for name, lines in sorted(files.items()):
        lesson = LESSON_ID.match(name)
        found = OPMODE_NAME.findall("\n".join(lines))
        if not (lesson and found):
            continue
        rows.setdefault(lesson.group(1), {"names": [], "tests": []})["names"] += found
    for name, lines in sorted(files.items()):
        lesson = LESSON_ID.match(name)
        file_id = lesson.group(1) if lesson else None
        for todo_id, test in named_tests(lines):
            owner = whose_test(test, file_id, todo_id, rows)
            if owner and test not in rows[owner]["tests"]:
                rows[owner]["tests"].append(test)
    out = ["| Lesson | On the robot | On the laptop |", "| --- | --- | --- |"]
    for lesson in sorted(rows, key=lambda k: (int(re.sub(r"\D", "", k)), k)):
        robot = " ".join(f"`{opmode}` ({kind})" for kind, opmode in rows[lesson]["names"])
        command = test_command(rows[lesson]["tests"], lesson)
        out.append(f"| L{lesson} | {robot} | {'`' + command + '`' if command else '--'} |")
    return out


def drivetrain_table(lines: list[str]) -> list[str]:
    """Which lesson writes which method of the shared drivetrain."""
    owner = None
    rows: dict[str, list[str]] = defaultdict(list)
    for line in lines:
        divider = DIVIDER.match(line)
        if divider:
            writes = WRITES.match(divider.group(1).strip())
            owner = writes.group(1) if writes else None
            continue
        signature = SIGNATURE.match(line)
        if owner and signature:
            rows[owner].append(signature.group(1))
    out = ["| Lesson | What it writes in `LessonsDriveTrain` |", "| --- | --- |"]
    for owner in sorted(rows, key=lambda k: (int(re.sub(r"\D", "", k.split()[0])), k)):
        methods = rows[owner]
        written = ", ".join(f"`{method}()`" for method in dict.fromkeys(methods))
        out.append(f"| {owner} | {written} |")
    return out


def publishes_table(files: dict[str, list[str]]) -> list[str]:
    """Every log key a lesson publishes, and where it comes from."""
    keys: dict[str, list[str]] = defaultdict(list)
    for name, lines in sorted(files.items()):
        body = "\n".join(lines)
        found = [key + "..." if key.endswith("/") else key for key in PUBLISH.findall(body)]
        # A shadow localizer publishes under its own name, from the class that
        # holds it, so the lesson's own line is where the name comes from.
        found += [f"Localizer/{shadow}/..." for shadow in SHADOW.findall(body)]
        for shown in found:
            if Path(name).stem not in keys[shown]:
                keys[shown].append(Path(name).stem)
    out = ["| Log key | Published by |", "| --- | --- |"]
    for key in sorted(keys):
        out.append(f"| `{key}` | {', '.join('`' + n + '`' for n in keys[key])} |")
    return out


def page(blank: dict[str, list[str]], filled: dict[str, list[str]],
         lessons: str, solutions: str) -> str:
    out = [
        "# Cheat sheet",
        "",
        "Every table here is read straight out of the lesson code, so it says what the code says.",
        "",
        "## What to run",
        "",
        "One OpMode name for the Driver Station, one command for the laptop. Each command runs only",
        "that lesson's own tests, which is what makes a red one easy to read. A dash means no test",
        "names that lesson: its check is on the floor.",
        "",
        *lessons_table(blank),
        "",
        "## Who writes what in the shared drivetrain",
        "",
        "Every lesson adds to one class, and nothing is written twice. A method written in an early",
        "lesson is the one a later lesson calls.",
        "",
        *drivetrain_table(blank["LessonsDriveTrain.java"]),
        "",
        "## What shows up in Panels",
        "",
        "A key ending in `...` has a name added to the end of it, one per wheel or per localizer.",
        "",
        *publishes_table(filled),
        "",
        f"Generated from the lessons line at `{lessons}` and the solutions line at `{solutions}`.",
        "",
    ]
    return "\n".join(out)


def generate() -> str:
    lessons, solutions = pins()
    return page(package(lessons), package(solutions), lessons, solutions)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true",
                        help="compare with what is committed instead of writing")
    args = parser.parse_args()

    out = book_root() / OUT
    text = generate()
    if args.check:
        if not out.exists():
            print(f"{OUT}: generated and missing. Run tools/cheatsheet.py.")
            return 1
        if out.read_text(encoding="utf-8") != text:
            print(f"{OUT}: differs from what the generator produces."
                  " Run tools/cheatsheet.py and commit what it writes.")
            return 1
        print("the cheat sheet matches the lesson code")
        return 0
    out.write_text(text, encoding="utf-8")
    print(f"wrote {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
