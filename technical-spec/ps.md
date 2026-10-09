# C++: Device Limit

A streaming plan allows a limited number of devices to be signed in at the same time. A device takes a slot when it signs in and frees the slot when it signs out, so a household can share one plan within its limit. If an extra device can sign in, switching from one device to another uses two slots, or a signed-out device keeps its slot, paying customers are locked out of their own plan.

Your task is to fix the device limit tool so that every command updates the data correctly and prints the correct success message or error reason.

## State File

**File: `data/plan.json` (read-only)**

```json
{
  "plan": "Family",
  "device_limit": 5
}
```

Fields:

- `plan` (string): the plan name.
- `device_limit` (integer): how many devices may be signed in at once.

**File: `data/sessions.json`**

```json
{
  "active": ["phone","tv","laptop","kids tablet","game console"]
}
```

Fields:

- `active` (list of strings): the devices signed in, in sign-in order.

Device names are compared exactly, and a name with spaces is quoted on the command line. When writing `data/sessions.json` back, preserve the field order shown above, and keep devices in the order they appear in the file.

## Command Output

Each command prints a one-line result on success, or `Error: <REASON>` on failure. Its rules are checked in the order listed, stopping at the first failure, and on failure `data/sessions.json` is not modified.

## Commands

### `login`

Signs a device in.

**Usage**

```bash
./devices login <device>
```

**Output**

```text
<device> signed in (<used>/<limit>)
```

**Rules**

Check these rules in order and stop at the first failure:

- If the device is already signed in, fail with `ALREADY_LOGGED_IN`.
- If `device_limit` devices are already signed in, fail with `DEVICE_LIMIT`.

On success:

- Append the device.
- `<used>` is the number signed in afterwards.

### `logout`

Signs a device out and frees its slot.

**Usage**

```bash
./devices logout <device>
```

**Output**

```text
<device> signed out (<used>/<limit>)
```

**Rules**

- If the device is not signed in, fail with `NOT_LOGGED_IN`.

On success, remove the device.

### `switch`

Replaces a signed-in device with another device in a single step.

**Usage**

```bash
./devices switch <old> <new>
```

**Output**

```text
Switched <old> to <new> (<used>/<limit>)
```

**Rules**

Check these rules in order and stop at the first failure:

- If `old` and `new` are the same name, fail with `SAME_DEVICE`.
- If `old` is not signed in, fail with `NOT_LOGGED_IN`.
- If `new` is already signed in, fail with `ALREADY_LOGGED_IN`.

On success:

- Replace `old` with `new` in the same position.
- The switch works even when the limit is full, because the old device leaves first.

> **Note:** Refer to `README.md` for more details and examples.
