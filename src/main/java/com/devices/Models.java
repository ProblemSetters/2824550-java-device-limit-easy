package com.devices;

import java.util.ArrayList;
import java.util.List;

public final class Models {
    private Models() {}

    public static final class Plan {
        public String name = "";
        public int deviceLimit = 0;

        public Plan() {}

        public Plan(String name, int deviceLimit) {
            this.name = name;
            this.deviceLimit = deviceLimit;
        }
    }

    public static final class Sessions {
        public List<String> active = new ArrayList<>();

        public Sessions() {}

        public Sessions(List<String> active) {
            this.active = new ArrayList<>(active);
        }
    }

    public enum Reason {
        NONE,
        ALREADY_LOGGED_IN,
        DEVICE_LIMIT,
        NOT_LOGGED_IN,
        SAME_DEVICE
    }

    public static final class CommandResult {
        public boolean ok = false;
        public String message = "";
        public Reason reason = Reason.NONE;

        public CommandResult() {}

        public CommandResult(boolean ok, String message, Reason reason) {
            this.ok = ok;
            this.message = message;
            this.reason = reason;
        }

        public static CommandResult success(String message) {
            return new CommandResult(true, message, Reason.NONE);
        }

        public static CommandResult failure(Reason reason) {
            return new CommandResult(false, "", reason);
        }
    }
}
