"""Plot the source size (lines, as counted by wc -l) of every approach.

usage: python3 benchmark/plot_sizes.py <out.png>
Run from the repository root; the files are located relative to this script.
"""
import pathlib
import subprocess
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Patch

out_png = sys.argv[1] if len(sys.argv) > 1 else "sizes.png"
repo = pathlib.Path(__file__).resolve().parent.parent

# label -> (family, source file or None for the one-liner)
VARIANTS = [
    ("find -exec cat {} + | wc -l", "Shell pipeline", None),
    ("Bash find + awk",             "Shell pipeline", "BashCountLines/find_and_awk.sh"),
    ("Python",                      "Interpreter / VM", "PythonCountLines/count_lines_in_files.py"),
    ("Java sequential",             "Interpreter / VM", "JavaCountLines/src/Main.java"),
    ("Java parallel",               "Interpreter / VM", "JavaCountLinesParallel/src/Main.java"),
    ("C++",                         "Compiled", "CppCountLines/count_lines.cpp"),
    ("C char-wise",                 "Compiled", "CCountLines/count_lines_in_files_char.c"),
    ("C block-wise",                "Compiled", "CCountLines/count_lines_in_files_block.c"),
    ("C raw read",                  "Compiled", "CCountLines/count_lines_in_files_raw.c"),
    ("Go",                          "Compiled", "GoCountLines/count_lines.go"),
    ("Rust",                        "Compiled", "RustCountLines/src/main.rs"),
]
COLOR = {"Shell pipeline": "#2a78d6", "Interpreter / VM": "#eb6834", "Compiled": "#1baf7a"}
TEXT, MUTED, GRID = "#0b0b0b", "#52514e", "#e4e3df"


def wc_l(path):
    return int(subprocess.run(["wc", "-l", str(path)], capture_output=True, text=True,
                              check=True).stdout.split()[0])


rows = []
for label, family, src in VARIANTS:
    n = 1 if src is None else wc_l(repo / src)
    rows.append((n, label, family))
rows.sort(reverse=True)

labels = [r[1] for r in rows]
sizes = [r[0] for r in rows]
colors = [COLOR[r[2]] for r in rows]

plt.rcParams.update({"font.family": "Helvetica", "font.size": 12})
fig, ax = plt.subplots(figsize=(11, 6.4), dpi=200)
fig.patch.set_facecolor("white")
ax.set_facecolor("white")

y = range(len(labels))
bars = ax.barh(y, sizes, color=colors, height=0.62, zorder=3)
ax.set_yticks(list(y))
ax.set_yticklabels(labels, color=TEXT)
ax.invert_yaxis()

xmax = max(sizes)
ax.set_xlim(0, xmax * 1.14)
for b, n in zip(bars, sizes):
    ax.text(b.get_width() + xmax * 0.01, b.get_y() + b.get_height() / 2,
            f"{n} {'line' if n == 1 else 'lines'}", va="center", ha="left",
            color=TEXT, fontsize=11)

ax.set_xlabel("Source size in lines, as reported by wc -l (lower is simpler)", color=MUTED)
ax.xaxis.grid(True, color=GRID, zorder=0)
ax.set_axisbelow(True)
for s in ("top", "right", "left"):
    ax.spines[s].set_visible(False)
ax.spines["bottom"].set_color(GRID)
ax.tick_params(axis="x", colors=MUTED, length=0)
ax.tick_params(axis="y", length=0)

ax.set_title("How much code does it take to count the lines?",
             loc="left", fontsize=15, fontweight="bold", color=TEXT, pad=22)
ax.text(0, 1.015, "Same task for every approach: all files matching a pattern below a folder, recursively",
        transform=ax.transAxes, color=MUTED, fontsize=11)

handles = [Patch(color=c, label=f) for f, c in COLOR.items()]
ax.legend(handles=handles, loc="lower right", frameon=False, fontsize=11)

fig.tight_layout()
fig.savefig(out_png, facecolor="white")
print("wrote", out_png)
for n, label, _ in rows:
    print(f"{label:30s} {n:4d}")
