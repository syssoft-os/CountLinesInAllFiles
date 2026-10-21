"""Plot median wall-clock runtimes from bench2.tsv as a horizontal bar chart."""
import csv
import statistics
import sys
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Patch

tsv, out_png = sys.argv[1], sys.argv[2]
subtitle = sys.argv[3] if len(sys.argv) > 3 else ""

runs = defaultdict(list)
with open(tsv, newline="") as f:
    for row in csv.DictReader(f, delimiter="\t"):
        runs[row["label"]].append(float(row["real"]))

# family of each variant -> categorical slot (fixed order, never cycled)
FAMILY = {
    "find -exec cat {} \\; | wc -l": "Shell pipeline",
    "find -exec cat {} + | wc -l": "Shell pipeline",
    "Bash find + awk": "Shell pipeline",
    "Python": "Interpreter / VM",
    "Java sequential": "Interpreter / VM",
    "Java parallel": "Interpreter / VM",
    "C++ -O0": "Compiled",
    "C++ -O3": "Compiled",
    "C char-wise -O0": "Compiled",
    "C char-wise -O3": "Compiled",
    "C block-wise 4 KB -O3": "Compiled",
    "C block-wise 64 KB -O3": "Compiled",
    "C raw read 64 KB -O3": "Compiled",
    "Go": "Compiled",
    "Rust --release": "Compiled",
}
COLOR = {"Shell pipeline": "#2a78d6", "Interpreter / VM": "#eb6834", "Compiled": "#1baf7a"}
TEXT, MUTED, GRID = "#0b0b0b", "#52514e", "#e4e3df"

rows = sorted(((statistics.median(v), k) for k, v in runs.items()), reverse=True)
labels = [k for _, k in rows]
medians = [m for m, _ in rows]
colors = [COLOR[FAMILY[k]] for k in labels]

plt.rcParams.update({"font.family": "Helvetica", "font.size": 12})
fig, ax = plt.subplots(figsize=(11, 7.8), dpi=200)
fig.patch.set_facecolor("white")
ax.set_facecolor("white")

y = range(len(labels))
bars = ax.barh(y, medians, color=colors, height=0.62, zorder=3)
ax.set_yticks(list(y))
ax.set_yticklabels(labels, color=TEXT)
ax.invert_yaxis()

xmax = max(medians)
ax.set_xlim(0, xmax * 1.14)
for b, m, k in zip(bars, medians, labels):
    lo, hi = min(runs[k]), max(runs[k])
    ax.text(b.get_width() + xmax * 0.01, b.get_y() + b.get_height() / 2,
            f"{m:.1f} s", va="center", ha="left", color=TEXT, fontsize=11)

ax.set_xlabel("Wall-clock time in seconds (median of 3 runs, lower is better)", color=MUTED)
ax.xaxis.grid(True, color=GRID, zorder=0)
ax.set_axisbelow(True)
for s in ("top", "right", "left"):
    ax.spines[s].set_visible(False)
ax.spines["bottom"].set_color(GRID)
ax.tick_params(axis="x", colors=MUTED, length=0)
ax.tick_params(axis="y", length=0)

ax.set_title("Counting 26 million lines in 36,929 C files of Linux 7.2.9",
             loc="left", fontsize=15, fontweight="bold", color=TEXT, pad=22)
if subtitle:
    ax.text(0, 1.015, subtitle, transform=ax.transAxes, color=MUTED, fontsize=11)

handles = [Patch(color=c, label=f) for f, c in COLOR.items()]
ax.legend(handles=handles, loc="lower right", frameon=False, fontsize=11)

fig.tight_layout()
fig.savefig(out_png, facecolor="white")
print("wrote", out_png)
for m, k in rows:
    v = runs[k]
    print(f"{k:32s} median {m:6.2f}  runs {', '.join(f'{x:.2f}' for x in v)}")
