# Java: Device Limit

Your task is to fix the device limit tool so that every command updates the data correctly and prints the correct success message or error reason. It reads the state from `data/plan.json` and `data/sessions.json`, processes the `login`, `logout` and `switch` commands, and writes the updated state back.

## Project Structure

```text
├── data/
│   ├── plan.json               # Read-only plan (plan, device_limit)
│   └── sessions.json           # Persistent sessions state (active)
├── src/
│   ├── main/java/com/devices/
│   │   ├── Commands.java       # Command handlers
│   │   ├── FileIo.java         # State file load and save
│   │   ├── Main.java           # CLI entry point
│   │   ├── Models.java         # Data models
│   │   └── Parser.java         # JSON parsing
│   └── test/java/com/devices/
│       └── CommandsTest.java
├── devices                     # CLI launcher script
├── run.sh                      # Setup and run script
├── reset.sh                    # Restore data/sessions.json to its initial state
├── test.sh                     # Test script (writes junit.xml)
├── pom.xml                     # Maven build configuration
└── README.md
```

## Commands

### Install

```bash
mvn -q -DskipTests package
```

### Run

```bash
bash run.sh
```

### Test

```bash
bash test.sh
```

### Reset

Restore `data/sessions.json` to its initial state.

```bash
bash reset.sh
```

## Examples

Each example below runs independently against the initial data files: the Family plan with a limit of 5, and five devices signed in: `phone`, `tv`, `laptop`, `kids tablet` and `game console`.

### Example 1: A sixth device is turned away while the plan is full

Command:

```bash
./devices login projector
```

Output:

```text
Error: DEVICE_LIMIT
```

The five devices on the list already take every slot on the plan, so there is no room for `projector`. `data/sessions.json` is not modified.

### Example 2: A device that is signed in cannot sign in again

Command:

```bash
./devices login phone
```

Output:

```text
Error: ALREADY_LOGGED_IN
```

`phone` is on the list already, so it is not signed in a second time. `data/sessions.json` is not modified.

### Example 3: Switching trades one device for another in a single step

Command:

```bash
./devices switch laptop projector
```

Output:

```text
Switched laptop to projector (5/5)
```

Final `data/sessions.json`:

```json
{"active":["phone","tv","projector","kids tablet","game console"]}
```

`laptop` is signed out and `projector` signed in by the one command. `projector` takes the position `laptop` had, and the other devices are untouched.

### Example 4: Signing out frees a slot for the next device

Commands:

```bash
./devices logout tv
./devices login projector
```

Output:

```text
tv signed out (4/5)
projector signed in (5/5)
```

Final `data/sessions.json`:

```json
{"active":["phone","laptop","kids tablet","game console","projector"]}
```

With `tv` signed out there is room on the plan for `projector`, which joins the end of the list.

### Example 5: Both ends of a switch are checked

Commands:

```bash
./devices switch watch tv
./devices switch phone tv
```

Output:

```text
Error: NOT_LOGGED_IN
Error: ALREADY_LOGGED_IN
```

`watch` is not signed in, so it cannot be the device that leaves. `tv` is signed in already, so it cannot be the device that arrives. Neither command changes the file.
