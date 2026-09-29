"""Cross-check the pencilled figures against the register, both ways.

A figure that has not been taken yet is a box in the page carrying its id and
what it will show. FIGURES.md is the register: one row per figure, with what it
must show, how it will be obtained, and either `pencilled` or the path of the
image.

Both directions are checked, because either one alone lets the guide lie: a box
with no row is a figure nobody is going to take, and a row with no box is a
promise the pages never make. The count of pencilled figures is never written
down anywhere; ask this check for it.
"""

import re
import sys

from bookpaths import book_root, relative, tracked

REGISTER = "FIGURES.md"

# ::: {admonition} fig-l2-sticks   /  :class: pencil
BOX = re.compile(r"^:{3,}\s*\{admonition\}\s*(?P<id>fig-[a-z0-9-]+)\s*$")
CLASS = re.compile(r"^\s*:class:\s*(?P<classes>.+?)\s*$")
ROW = re.compile(r"^\|\s*`(?P<id>fig-[a-z0-9-]+)`\s*\|(?P<rest>.*)\|\s*$")


def boxes() -> dict[str, str]:
    """Pencilled figure id to the page it sits in."""
    found = {}
    for path in tracked("source/*.md", "source/**/*.md"):
        lines = path.read_text(encoding="utf-8").splitlines()
        for n, line in enumerate(lines):
            match = BOX.match(line)
            if not match:
                continue
            following = lines[n + 1] if n + 1 < len(lines) else ""
            classes = CLASS.match(following)
            if classes and "pencil" in classes.group("classes").split():
                found[match.group("id")] = relative(path)
    return found


def rows() -> dict[str, str]:
    """Registered figure id to its status: `pencilled` or an image path."""
    found = {}
    for line in (book_root() / REGISTER).read_text(encoding="utf-8").splitlines():
        match = ROW.match(line)
        if not match:
            continue
        cells = [cell.strip() for cell in match.group("rest").split("|")]
        found[match.group("id")] = cells[-1].strip("`")
    return found


def main() -> int:
    drawn = boxes()
    registered = rows()
    problems = 0

    for figure, page in sorted(drawn.items()):
        if figure not in registered:
            print(f"{page}: {figure} is pencilled in and not in {REGISTER}")
            problems += 1
        elif registered[figure] != "pencilled":
            print(f"{page}: {figure} is pencilled in, and {REGISTER} says it exists as"
                  f" {registered[figure]}")
            problems += 1

    for figure, status in sorted(registered.items()):
        if status == "pencilled":
            if figure not in drawn:
                print(f"{REGISTER}: {figure} is pencilled and no page shows a box for it")
                problems += 1
        elif not (book_root() / status).exists():
            print(f"{REGISTER}: {figure} names {status}, which does not exist")
            problems += 1

    if problems:
        print(f"\n{problems} figure(s) out of step between the pages and {REGISTER}.")
        return 1
    print(f"{len(drawn)} figure(s) pencilled in, "
          f"{sum(1 for s in registered.values() if s != 'pencilled')} taken.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
