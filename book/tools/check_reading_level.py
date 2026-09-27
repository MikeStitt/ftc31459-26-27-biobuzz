"""Fail on a student page whose prose reads above grade 8.

The Constitution requires grade 8 or below of student-facing text, so this
fails rather than reports: the pages under source/ are what a student reads.
The guide's own working documents are not student-facing and are not measured.

Whole-file scores hide the paragraph that is wrong, so this scores paragraph by
paragraph. A paragraph under 20 words is skipped: the formulas are unreliable
on one clause, and a run of short UI labels scores high and reads fine.
"""

import re
import sys

import textstat

from bookpaths import relative, tracked

THRESHOLD = 8.0
MIN_WORDS = 20

MARKUP = re.compile(r"(``?[^`]*``?|\*\*?|__?|\[|\]\([^)]*\))")
DIRECTIVE = re.compile(r"^\s*(:{3,}|`{3,}|:[a-z_]+:|\{[a-z]+\}|#|\||-{3,})")


def paragraphs(path):
    """Yield (first line number, text) for each prose paragraph."""
    lines = open(path, encoding="utf-8").read().splitlines()
    buf: list[str] = []
    start = 1
    in_fence = False
    for n, line in enumerate(lines, start=1):
        if line.lstrip().startswith("```") or line.lstrip().startswith(":::"):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        if not line.strip():
            if buf:
                yield start, " ".join(buf)
                buf = []
            continue
        if DIRECTIVE.match(line):
            continue
        if not buf:
            start = n
        buf.append(line.strip())
    if buf:
        yield start, " ".join(buf)


def main() -> int:
    hits = []
    for path in tracked("source/*.md", "source/**/*.md"):
        for line_no, text in paragraphs(path):
            plain = MARKUP.sub(" ", text).strip()
            if len(plain.split()) < MIN_WORDS:
                continue
            grade = textstat.text_standard(plain, float_output=True)
            if grade > THRESHOLD:
                hits.append((relative(path), line_no, grade, plain))
    for page, line_no, grade, plain in hits:
        print(f"{page}:{line_no}: grade {grade:.1f}")
        print(f"    {plain[:90]}")
    if hits:
        print(f"\n{len(hits)} paragraph(s) above grade {THRESHOLD:.0f}.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
