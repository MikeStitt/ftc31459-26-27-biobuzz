"""Runs one check, with tools/ importable, on any platform.

`ninja` hands its commands to `sh` on Unix and to `cmd` on Windows, and
`PYTHONPATH=tools python ...` is shell syntax that `cmd` reads as the name of a
program. So the path is set here instead, in the one file that needs to edit it,
rather than in eight rules that cannot be written portably.

Usage: python tools/run.py check_wrap [args...]
"""

import runpy
import sys
from pathlib import Path


def main() -> int:
    if len(sys.argv) < 2:
        print("run.py: name a module under tools/, for example check_wrap")
        return 2
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    module = sys.argv[1]
    sys.argv = [module, *sys.argv[2:]]
    runpy.run_module(module, run_name="__main__")
    return 0


if __name__ == "__main__":
    sys.exit(main())
