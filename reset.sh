#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cat > "$SCRIPT_DIR/data/sessions.json" <<'EOF'
{
  "active":["phone","tv","laptop","kids tablet","game console"]
}
EOF

echo "Restored data/sessions.json to its initial state"
