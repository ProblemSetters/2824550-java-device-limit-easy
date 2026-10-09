#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p data

mvn -B -ntp -DskipTests compile 2>&1

echo ""
echo "State files: data/plan.json (read-only), data/sessions.json"
echo ""
echo "Usage:"
echo "  ./devices login <device>"
echo "  ./devices logout <device>"
echo "  ./devices switch <old> <new>"
echo ""
echo "Reset:"
echo "  bash reset.sh"
