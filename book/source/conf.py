"""Sphinx configuration for the lessons how-to guide.

MyST Markdown and Furo, the same pair corbelsflightlog's docs use, so a page
can move between the two without conversion and a student sees one shape here,
there and on gm0.org.

Built with -W: a broken link or a missing image fails the build rather than
printing a warning nobody reads.
"""

project = "Biobuz lessons"
author = "Catholic Central Spires Robotics"
copyright = "2026, Catholic Central Spires Robotics"

extensions = ["myst_parser"]
myst_enable_extensions = ["colon_fence", "deflist"]

exclude_patterns = ["_build", "Thumbs.db", ".DS_Store"]

html_theme = "furo"
html_title = "Biobuz lessons"
html_static_path = ["_static"]
html_css_files = ["pencil.css"]
