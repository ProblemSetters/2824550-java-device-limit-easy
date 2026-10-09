package com.devices;

import com.devices.Models.Sessions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileIo {
    private FileIo() {}

    /// Escape a string for safe inclusion inside JSON double quotes.
    public static String escapeJsonString(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(ch);
            }
        }
        return escaped.toString();
    }

    /// Read a text file as a single string.
    public static String readFileToString(String path) {
        try {
            return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open file: " + path, e);
        }
    }

    /// Format the signed-in devices as JSON for data/sessions.json.
    public static String sessionsToJson(Sessions sessions) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n  \"active\":[");
        for (int i = 0; i < sessions.active.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"').append(escapeJsonString(sessions.active.get(i))).append('"');
        }
        builder.append("]\n}");
        return builder.toString();
    }

    /// Write the signed-in devices JSON to the given file.
    public static void writeSessionsJson(String path, Sessions sessions) {
        try {
            Files.writeString(Paths.get(path), sessionsToJson(sessions) + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open output file: " + path, e);
        }
    }
}
