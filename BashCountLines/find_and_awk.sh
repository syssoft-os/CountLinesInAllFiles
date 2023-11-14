#!/bin/bash

folder=$1
regex=$2

total_lines=$(find "$folder" -type f -name "$regex" -print0 | xargs -0 awk 'END{print NR}')

echo "Total number of lines: $total_lines"

