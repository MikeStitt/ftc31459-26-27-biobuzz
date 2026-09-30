"""Where the guide's files are, for every check under tools/.

One place resolves paths, so a check never guesses at the working directory it
was run from. Everything is relative to book/, which is where `ninja book`
runs.
"""

import subprocess
from pathlib import Path


def book_root() -> Path:
    """The book/ directory, whatever directory a check was started from."""
    return Path(__file__).resolve().parent.parent


def tracked(*globs: str) -> list[Path]:
    """Tracked files under book/ matching the globs, as absolute paths.

    Tracked, not on-disk: an untracked draft is nobody's gate yet, and a build
    directory full of generated HTML is not prose to check.
    """
    root = book_root()
    out = subprocess.run(
        ["git", "ls-files", "--", *globs],
        capture_output=True,
        text=True,
        check=True,
        cwd=root,
    ).stdout
    return [root / line for line in out.splitlines()]


def relative(path: Path) -> str:
    """A path as the checks print it and compare it: relative to book/, with
    forward slashes.

    as_posix(), not str(): on Windows str() gives `source\\tasks\\l9.md`, which
    matches nothing in keep.toml, and every page read as having no ceiling.
    """
    return Path(path).resolve().relative_to(book_root()).as_posix()
