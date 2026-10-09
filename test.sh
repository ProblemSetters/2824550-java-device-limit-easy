#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Remove stale surefire report so a failed build cannot reuse old results.
surefire_xml="target/surefire-reports/TEST-com.devices.CommandsTest.xml"
rm -f "$surefire_xml"

# Run Maven tests (allow failure so we can parse results).
mvn -B -ntp test 2>&1 || true

if [[ -f "$surefire_xml" ]]; then
    cp "$surefire_xml" junit.xml
else
    # Fallback: generate a minimal junit.xml indicating all tests failed.
    cat > junit.xml <<'XMLEOF'
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.devices.CommandsTest" tests="6" failures="6">
  <testcase name="test_login_signs_a_device_in_and_reports_the_new_count"><failure message="build failed" /></testcase>
  <testcase name="test_login_and_switch_rejections_use_the_stated_reason"><failure message="build failed" /></testcase>
  <testcase name="test_logout_frees_the_slot_and_keeps_the_order"><failure message="build failed" /></testcase>
  <testcase name="test_switch_replaces_the_device_in_its_place"><failure message="build failed" /></testcase>
  <testcase name="test_switch_works_when_the_plan_is_full"><failure message="build failed" /></testcase>
  <testcase name="test_switch_refuses_a_device_switched_for_itself"><failure message="build failed" /></testcase>
</testsuite>
XMLEOF
fi

# Print colored terminal output from junit.xml.
PYTHON_BIN="python3"
if ! "$PYTHON_BIN" --version >/dev/null 2>&1; then
    PYTHON_BIN="python"
fi

"$PYTHON_BIN" - <<'PY' || true
import xml.etree.ElementTree as ET, sys

try:
    tree = ET.parse("junit.xml")
except Exception:
    print("\033[1;31mCould not parse junit.xml\033[0m")
    sys.exit(0)

root = tree.getroot()
suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")

total = 0
failures = 0

for suite in suites:
    for tc in suite.findall("testcase"):
        name = tc.get("name", "unknown")
        total += 1
        fail = tc.find("failure")
        err = tc.find("error")
        problem = fail if fail is not None else err
        if problem is not None:
            failures += 1
            print(f"\033[1;31mFAIL: {name}\033[0m")
            msg = problem.get("message", "") or problem.text or ""
            for line in msg.splitlines():
                stripped = line.strip()
                if stripped:
                    print(f"       {stripped}")
        else:
            print(f"\033[32mPASS: {name}\033[0m")

if failures > 0:
    print(f"\n\033[1;31m{failures} test(s) failed.\033[0m")
else:
    print(f"\n\033[32mAll {total} tests passed!\033[0m")
PY
