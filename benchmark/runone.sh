#!/bin/bash
# Time a single command and print one TSV row: label, real, user, sys, result.
# usage: runone.sh <label> <command...>
label=$1; shift
err=$(mktemp)
out=$( { /usr/bin/time -p "$@" 2>"$err"; } | tail -1 | tr -dc '0-9')
read -r _ real < <(grep '^real' "$err")
read -r _ user < <(grep '^user' "$err")
read -r _ sys  < <(grep '^sys'  "$err")
rm -f "$err"
printf '%s\t%s\t%s\t%s\t%s\n' "$label" "$real" "$user" "$sys" "$out"
