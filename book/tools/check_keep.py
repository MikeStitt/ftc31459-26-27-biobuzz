"""Check that every passage in the register still reads as it was accepted.

A kept passage is one Mike has said he likes. The register holds the page, the
first line of the passage as an anchor, and the sha256 of the whole passage.
Rewriting one is possible and loud: Mike says so, and the register records the
new hash and date.

A passage is the block of non-blank lines the anchor sits in, which is how the
file already separates one thought from the next.
"""

import hashlib
import sys

from bookpaths import book_root
from register import PATH, kept


def passage(lines: list[str], anchor: str) -> str | None:
    """The block of non-blank lines holding the anchor, or None."""
    for n, line in enumerate(lines):
        if anchor not in line:
            continue
        start = n
        while start > 0 and lines[start - 1].strip():
            start -= 1
        end = n
        while end + 1 < len(lines) and lines[end + 1].strip():
            end += 1
        return "\n".join(line.rstrip() for line in lines[start:end + 1]).strip()
    return None


def main() -> int:
    problems = 0
    for entry in kept():
        page = entry["page"]
        path = book_root() / page
        if not path.exists():
            print(f"{PATH}: {page} does not exist, and holds a kept passage")
            problems += 1
            continue
        lines = path.read_text(encoding="utf-8").splitlines()
        found = passage(lines, entry["anchor"])
        if found is None:
            print(f"{PATH}: {page} no longer holds a passage starting \"{entry['anchor']}\"")
            problems += 1
            continue
        digest = hashlib.sha256(found.encode("utf-8")).hexdigest()
        if digest != entry["sha256"]:
            print(f"{page}: the passage \"{entry['anchor'][:50]}\" has been rewritten")
            print(f"    accepted {entry['date']} as {entry['sha256'][:16]}, now {digest[:16]}")
            problems += 1
    if problems:
        print(f"\n{problems} kept passage(s) moved. Mike accepts the new words, then the"
              f" register records the new hash and date.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
