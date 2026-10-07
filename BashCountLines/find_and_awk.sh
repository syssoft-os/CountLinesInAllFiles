#!/bin/bash

folder=$1
regex=$2

# xargs may split a long file list into several awk invocations, each printing
# its own END{...} result. A second awk therefore sums these partial counts.
total_lines=$(find "$folder" -type f -name "$regex" -print0 \
    | xargs -0 awk 'END{print NR}' \
    | awk '{sum += $1} END{print sum + 0}')

echo "Total number of lines: $total_lines"
