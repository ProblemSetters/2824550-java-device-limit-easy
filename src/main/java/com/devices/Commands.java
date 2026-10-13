package com.devices;

import com.devices.Models.CommandResult;
import com.devices.Models.Plan;
import com.devices.Models.Reason;
import com.devices.Models.Sessions;

public final class Commands {
    private Commands() {}

    /// Find the index of the signed-in device with the given name.
    private static int findDevice(Sessions sessions, String device) {
        for (int i = 0; i < sessions.active.size(); i++) {
            if (sessions.active.get(i).equals(device)) {
                return i;
            }
        }
        return -1;
    }

    /// Report whether the device is already signed in.
    private static boolean isSignedIn(Sessions sessions, String device) {
        return findDevice(sessions, device) != -1;
    }

    /// Count the devices signed in right now.
    private static int signedInCount(Sessions sessions) {
        return sessions.active.size();
    }

    /// Count how many more devices the plan lets in.
    private static int freeSlots(Sessions sessions, Plan plan) {
        return plan.deviceLimit - signedInCount(sessions);
    }

    /// Show how many devices are signed in against the plan's limit.
    private static String formatUsage(int used, int limit) {
        return "(" + used + "/" + limit + ")";
    }

    /// Build a successful result carrying the command's output message.
    private static CommandResult success(String message) {
        return CommandResult.success(message);
    }

    /// Build a failed result carrying the reason code.
    private static CommandResult failure(Reason reason) {
        return CommandResult.failure(reason);
    }

    /// Sign a device in to the plan.
    public static CommandResult login(Sessions sessions, Plan plan, String device) {
        if (!sessions.active.isEmpty() && sessions.active.get(sessions.active.size() - 1).equals(device)) {
            return failure(Reason.ALREADY_LOGGED_IN);
        }
        int slots = freeSlots(sessions, plan);
        if (slots == 0) {
            return failure(Reason.DEVICE_LIMIT);
        }

        sessions.active.add(device);

        return success(device + " signed in " +
                       formatUsage(plan.deviceLimit - slots, plan.deviceLimit));
    }

    /// Sign a device out of the plan.
    public static CommandResult logout(Sessions sessions, Plan plan, String device) {
        int index = findDevice(sessions, device);
        if (index == -1) {
            return failure(Reason.NOT_LOGGED_IN);
        }

        sessions.active.set(index, sessions.active.get(sessions.active.size() - 1));
        sessions.active.remove(sessions.active.size() - 1);

        return success(device + " signed out " +
                       formatUsage(signedInCount(sessions), plan.deviceLimit));
    }

    /// Replace one signed-in device with another in a single step.
    public static CommandResult switchDevice(Sessions sessions, Plan plan, String from, String to) {
        int fromIndex = findDevice(sessions, from);
        if (fromIndex == -1) {
            return failure(Reason.NOT_LOGGED_IN);
        }
        if (from.equals(to) || isSignedIn(sessions, to)) {
            return failure(Reason.ALREADY_LOGGED_IN);
        }
        if (freeSlots(sessions, plan) == 0) {
            return failure(Reason.DEVICE_LIMIT);
        }

        sessions.active.remove(fromIndex);
        sessions.active.add(to);

        return success("Switched " + from + " to " + to + " " +
                       formatUsage(signedInCount(sessions), plan.deviceLimit));
    }
}
