"""Check every page against its word ceiling, and that it has one.

Prose written by an agent gets longer every pass and nothing pulls it back, so
each page carries a ceiling set when the page was accepted. Raising one is a
visible line in a diff.

A page with no entry in the register fails too: a new page states its ceiling in
the commit that adds it.
"""

import sys

from bookpaths import relative, tracked
from register import PATH, pages

# Generated, and not prose: source/answers/ is what tools/answers.py writes out of
# the two lesson lines, and source/tasks/cheatsheet.md is what tools/cheatsheet.py
# reads out of them. A ceiling on a page nobody writes by hand would be a ceiling
# on how much code a lesson may have, or how many log keys it may publish.
GENERATED = ("source/answers/", "source/tasks/cheatsheet.md")


def words(path) -> int:
    """Words of prose: fenced code and MyST directives do not count."""
    total = 0
    in_fence = False
    for line in open(path, encoding="utf-8"):
        stripped = line.strip()
        if stripped.startswith("```") or stripped.startswith(":::"):
            in_fence = not in_fence
            continue
        if in_fence or stripped.startswith(":"):
            continue
        total += len(stripped.split())
    return total


def main() -> int:
    ceilings = pages()
    problems = 0
    for path in tracked("source/*.md", "source/**/*.md"):
        page = relative(path)
        if page.startswith(GENERATED):
            continue
        count = words(path)
        if page not in ceilings:
            print(f"{page}: {count} words and no ceiling. Add one to {PATH}.")
            problems += 1
            continue
        if count > ceilings[page]:
            print(f"{page}: {count} words, ceiling {ceilings[page]}")
            problems += 1
    if problems:
        print(f"\n{problems} page(s) over a ceiling or missing one.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
