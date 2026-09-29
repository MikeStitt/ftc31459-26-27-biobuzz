"""Check that the guide's Markdown is wrapped at 100 columns.

The same rule as the workspace's `tools/check_wrap.py`, over the guide's own
files: tables, fenced code and a line holding one unbreakable token are exempt,
because breaking those changes what they say.
"""

import sys

from bookpaths import relative, tracked

LIMIT = 100


def violations(path) -> list[tuple[int, int]]:
    found = []
    in_fence = False
    with open(path, encoding="utf-8") as fh:
        for n, line in enumerate(fh, start=1):
            line = line.rstrip("\n")
            if line.lstrip().startswith("```"):
                in_fence = not in_fence
                continue
            if in_fence or line.startswith("|"):
                continue
            if len(line.split()) == 1:
                continue
            if len(line) > LIMIT:
                found.append((n, len(line)))
    return found


def main() -> int:
    total = 0
    for path in tracked("*.md"):
        for line_no, length in violations(path):
            print(f"{relative(path)}:{line_no}: {length} columns")
            total += 1
    if total:
        print(f"\n{total} line(s) over {LIMIT} columns.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
