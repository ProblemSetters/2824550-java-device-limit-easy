package com.devices;

import com.devices.Models.CommandResult;
import com.devices.Models.Plan;
import com.devices.Models.Sessions;

import java.io.PrintStream;

public final class Main {
    private static final String PLAN_PATH = "data/plan.json";
    private static final String SESSIONS_PATH = "data/sessions.json";

    private Main() {}

    /// Print the supported commands and their arguments.
    private static void printUsage(PrintStream out) {
        out.print("Usage:\n" +
                  "  devices login <device>\n" +
                  "  devices logout <device>\n" +
                  "  devices switch <old> <new>\n");
    }

    /// Load the plan and the signed-in devices, dispatch the requested command, and write any changes back.
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage(System.err);
            System.exit(1);
        }

        String command = args[0];

        if ("login".equals(command)) {
            if (args.length != 2) {
                printUsage(System.err);
                System.exit(1);
            }
        } else if ("logout".equals(command)) {
            if (args.length != 2) {
                printUsage(System.err);
                System.exit(1);
            }
        } else if ("switch".equals(command)) {
            if (args.length != 3) {
                printUsage(System.err);
                System.exit(1);
            }
        } else {
            System.err.println("Unknown command: " + command);
            printUsage(System.err);
            System.exit(1);
        }

        try {
            Plan plan = Parser.parsePlanJson(FileIo.readFileToString(PLAN_PATH));
            Sessions sessions = Parser.parseSessionsJson(FileIo.readFileToString(SESSIONS_PATH));

            CommandResult result;
            if ("login".equals(command)) {
                result = Commands.login(sessions, plan, args[1]);
            } else if ("logout".equals(command)) {
                result = Commands.logout(sessions, plan, args[1]);
            } else {
                result = Commands.switchDevice(sessions, plan, args[1], args[2]);
            }

            if (result.ok) {
                FileIo.writeSessionsJson(SESSIONS_PATH, sessions);
                System.out.println(result.message);
            } else {
                System.out.println("Error: " + result.reason);
            }
        } catch (Exception error) {
            System.err.println("Error: " + error.getMessage());
            System.exit(1);
        }
    }
}
