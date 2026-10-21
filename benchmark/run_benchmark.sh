#!/bin/bash
# Build every variant and time it against a source tree, one program at a time.
# usage: benchmark/run_benchmark.sh <linux-source-dir> [repetitions] [out.tsv]
# Afterwards: python3 benchmark/plot_runtimes.py out.tsv runtimes.png "<subtitle>"
set -eu
L=${1:?usage: run_benchmark.sh <source-dir> [repetitions] [out.tsv]}
REPS=${2:-3}
OUT=${3:-benchmark/runtimes.tsv}
REPO=$(cd "$(dirname "$0")/.." && pwd)
B=$(mktemp -d)
cd "$REPO"

gcc -O3 -o "$B/c_char"     CCountLines/count_lines_in_files_char.c
gcc     -o "$B/c_char_O0"  CCountLines/count_lines_in_files_char.c
gcc -O3 -o "$B/c_block"    CCountLines/count_lines_in_files_block.c
gcc -O3 -DBUFFER_SIZE=65536 -o "$B/c_block64" CCountLines/count_lines_in_files_block.c
g++ -O3 -std=c++17 -o "$B/cpp"    CppCountLines/count_lines.cpp
g++     -std=c++17 -o "$B/cpp_O0" CppCountLines/count_lines.cpp
mkdir -p "$B/java" "$B/javap"
javac -d "$B/java"  JavaCountLines/src/Main.java
javac -d "$B/javap" JavaCountLinesParallel/src/Main.java
go build -o "$B/go" GoCountLines/count_lines.go
(cd RustCountLines && cargo build --release -q)
cp RustCountLines/target/release/count_lines "$B/rust"
printf '#!/bin/bash\nfind "$1" -type f -name "*.c" -exec cat {} \\; | wc -l\n' > "$B/oneliner_semicolon.sh"
printf '#!/bin/bash\nfind "$1" -type f -name "*.c" -exec cat {} + | wc -l\n'   > "$B/oneliner_plus.sh"
chmod +x "$B"/oneliner_*.sh

# warm the file cache once so every variant sees the same conditions
find "$L" -type f -name "*.c" -exec cat {} + > /dev/null

R=benchmark/runone.sh
printf 'label\treal\tuser\tsys\tresult\n' > "$OUT"
for rep in $(seq "$REPS"); do
  "$R" 'find -exec cat {} \; | wc -l' "$B/oneliner_semicolon.sh" "$L"                    >> "$OUT"; sleep 2
  "$R" 'find -exec cat {} + | wc -l'  "$B/oneliner_plus.sh" "$L"                         >> "$OUT"; sleep 2
  "$R" 'Bash find + awk'              bash BashCountLines/find_and_awk.sh "$L" "*.c"      >> "$OUT"; sleep 2
  "$R" 'Python'                       python3 PythonCountLines/count_lines_in_files.py "$L" '.*\.c$' >> "$OUT"; sleep 2
  "$R" 'Java sequential'              java -cp "$B/java"  Main "$L" '.*\.c$'              >> "$OUT"; sleep 2
  "$R" 'Java parallel'                java -cp "$B/javap" Main "$L" '.*\.c$'              >> "$OUT"; sleep 2
  "$R" 'C++ -O0'                      "$B/cpp_O0"    "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'C++ -O3'                      "$B/cpp"       "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'C char-wise -O0'              "$B/c_char_O0" "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'C char-wise -O3'              "$B/c_char"    "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'C block-wise 4 KB -O3'        "$B/c_block"   "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'C block-wise 64 KB -O3'       "$B/c_block64" "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'Go'                           "$B/go"        "$L" '.*\.c$'                        >> "$OUT"; sleep 2
  "$R" 'Rust --release'               "$B/rust"      "$L" '.*\.c$'                        >> "$OUT"; sleep 2
done
rm -rf "$B"
echo "wrote $OUT"
