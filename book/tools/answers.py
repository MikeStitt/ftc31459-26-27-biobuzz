"""Generate the answer pages from the two lesson lines.

For every lesson file the lessons line marks with a TODO, this reads the same
path from the solutions line and writes what fills each blank. Both files come
out of git rather than the working tree, so the answers are pinned to the
commits keep.toml names and a regeneration gives the same pages.

Aligning the two files with difflib rather than parsing Java is deliberate: a
blank is wherever the two lines differ, which is exactly what the student has to
write. Every difference is emitted, and one that carries no TODO is labelled as
such, so a page cannot quietly leave a blank out.

Run it with --check to compare against what is committed instead of writing.
"""

import argparse
import difflib
import subprocess
import sys
from pathlib import Path

from bookpaths import book_root
from register import load

LESSONS_DIR = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons"
OUT = "source/answers"


def pins() -> tuple[str, str]:
    answers = load()["answers"]
    return answers["lessons_commit"], answers["solutions_commit"]


def git(*args: str) -> str:
    return subprocess.run(
        ["git", *args], capture_output=True, text=True, check=True,
        cwd=book_root().parent,
    ).stdout


def lesson_files(lessons: str) -> list[str]:
    listed = git("ls-tree", "-r", "--name-only", lessons, LESSONS_DIR + "/").splitlines()
    return [path for path in listed if "TODO" in git("show", f"{lessons}:{path}")]


def todo_numbers(block: list[str]) -> str:
    """How a block of lessons-line lines names itself, for the heading."""
    numbers = []
    for line in block:
        marker = line.find("TODO")
        if marker < 0:
            continue
        rest = line[marker + 4:].strip().rstrip(":")
        numbers.append("TODO " + rest.split(":")[0].strip() if rest else "TODO")
    if not numbers:
        return "An unmarked difference"
    return numbers[0]


def only_comment(block: list[str]) -> bool:
    """Is this block nothing but comment lines?

    The two lines differ in javadoc as well as in code: a lesson file says
    `Passes when:` and the solutions file does not. That is not a blank a student
    fills in, so a difference made only of comments and carrying no TODO is
    counted and not printed.
    """
    for line in block:
        stripped = line.strip()
        if not stripped:
            continue
        if not stripped.startswith(("*", "/*", "*/", "//")):
            return False
    return True


def fence(block: list[str]) -> str:
    body = "\n".join(line.rstrip() for line in block) or "(nothing)"
    return f"```java\n{body}\n```"


def page(path: str, blank: list[str], filled: list[str], solutions: str) -> str:
    name = Path(path).stem
    out = [f"# {name}", ""]
    out.append(f"The blanks in this file, filled in from `{solutions}`:")
    out.append("")
    out.append(f"`{path}`")
    out.append("")
    matcher = difflib.SequenceMatcher(None, blank, filled, autojunk=False)
    count = 0
    comments = 0
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag == "equal":
            continue
        if ("TODO" not in "".join(blank[i1:i2])
                and only_comment(blank[i1:i2]) and only_comment(filled[j1:j2])):
            comments += 1
            continue
        count += 1
        out.append(f"## {todo_numbers(blank[i1:i2])}")
        out.append("")
        out.append("What the lesson leaves blank:")
        out.append("")
        out.append(fence(blank[i1:i2]))
        out.append("")
        out.append("What the solutions line has there:")
        out.append("")
        out.append(fence(filled[j1:j2]))
        out.append("")
    if not count:
        out.append("No blank in this file: nothing but comments differs between the two lines.")
        out.append("")
    if comments:
        out.append(f"The two lines also differ in {comments} run(s) of comment lines, which are not")
        out.append("blanks and are not shown.")
        out.append("")
    return "\n".join(out)


def index(names: list[str]) -> str:
    entries = "\n".join(names)
    return (
        "# Answers\n"
        "\n"
        "The real code, out of the solutions line. Use it when you are stuck, not instead of being\n"
        "stuck.\n"
        "\n"
        "```{toctree}\n"
        ":maxdepth: 1\n"
        "\n"
        f"{entries}\n"
        "```\n"
    )


def generate() -> dict[str, str]:
    lessons, solutions = pins()
    pages = {}
    for path in lesson_files(lessons):
        blank = git("show", f"{lessons}:{path}").splitlines()
        filled = git("show", f"{solutions}:{path}").splitlines()
        pages[Path(path).stem + ".md"] = page(path, blank, filled, solutions)
    pages["index.md"] = index(sorted(name[:-3] for name in pages))
    return pages


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true",
                        help="compare with what is committed instead of writing")
    args = parser.parse_args()

    out = book_root() / OUT
    pages = generate()

    if args.check:
        problems = 0
        on_disk = {path.name for path in out.glob("*.md")} if out.exists() else set()
        for name in sorted(set(pages) | on_disk):
            if name not in pages:
                print(f"{OUT}/{name}: not generated any more, and still committed")
                problems += 1
            elif name not in on_disk:
                print(f"{OUT}/{name}: generated and missing. Run tools/answers.py.")
                problems += 1
            elif (out / name).read_text(encoding="utf-8") != pages[name]:
                print(f"{OUT}/{name}: differs from what the generator produces")
                problems += 1
        if problems:
            print(f"\n{problems} answer page(s) out of step with the lesson lines."
                  f" Run tools/answers.py and commit what it writes.")
            return 1
        print(f"{len(pages) - 1} answer page(s) match the two lines.")
        return 0

    out.mkdir(parents=True, exist_ok=True)
    for name, text in pages.items():
        (out / name).write_text(text, encoding="utf-8")
    print(f"wrote {len(pages)} page(s) into {OUT}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
