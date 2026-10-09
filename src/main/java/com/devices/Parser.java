package com.devices;

import com.devices.Models.Plan;
import com.devices.Models.Sessions;

public final class Parser {
    private Parser() {}

    private static final class JsonParser {
        private final String json;
        private int pos = 0;

        JsonParser(String json) {
            this.json = json != null ? json : "";
        }

        Plan parsePlanDoc() {
            Plan plan = new Plan();
            skipWs();
            expect('{');
            while (true) {
                skipWs();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                if ("plan".equals(key)) {
                    plan.name = parseString();
                } else if ("device_limit".equals(key)) {
                    plan.deviceLimit = parseInt();
                }
                skipWs();
                if (peek() == ',') {
                    pos++;
                }
            }
            return plan;
        }

        Sessions parseSessionsDoc() {
            Sessions sessions = new Sessions();
            skipWs();
            expect('{');
            while (true) {
                skipWs();
                if (peek() == '}') {
                    pos++;
                    break;
                }
                String key = parseString();
                skipWs();
                expect(':');
                skipWs();
                if ("active".equals(key)) {
                    parseArray(() -> sessions.active.add(parseString()));
                }
                skipWs();
                if (peek() == ',') {
                    pos++;
                }
            }
            return sessions;
        }

        private void parseArray(Runnable parseElement) {
            expect('[');
            skipWs();
            if (peek() == ']') {
                pos++;
                return;
            }
            while (true) {
                parseElement.run();
                skipWs();
                if (peek() == ',') {
                    pos++;
                    skipWs();
                    continue;
                }
                expect(']');
                break;
            }
        }

        private char peek() {
            return pos < json.length() ? json.charAt(pos) : '\0';
        }

        private void expect(char c) {
            if (peek() != c) {
                throw new RuntimeException("JSON parse error: expected '" + c + "' at position " + pos);
            }
            pos++;
        }

        private void skipWs() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (pos < json.length() && json.charAt(pos) != '"') {
                if (json.charAt(pos) == '\\' && pos + 1 < json.length()) {
                    pos++;
                    char escaped = json.charAt(pos++);
                    switch (escaped) {
                        case 'n' -> value.append('\n');
                        case 'r' -> value.append('\r');
                        case 't' -> value.append('\t');
                        default -> value.append(escaped);
                    }
                } else {
                    value.append(json.charAt(pos++));
                }
            }
            expect('"');
            return value.toString();
        }

        private int parseInt() {
            if (!Character.isDigit(peek())) {
                throw new RuntimeException("JSON parse error: expected integer at position " + pos);
            }
            int value = 0;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) {
                value = value * 10 + (json.charAt(pos) - '0');
                pos++;
            }
            return value;
        }
    }

    /// Parse the read-only plan JSON.
    public static Plan parsePlanJson(String doc) {
        return new JsonParser(doc).parsePlanDoc();
    }

    /// Parse the signed-in devices JSON.
    public static Sessions parseSessionsJson(String doc) {
        return new JsonParser(doc).parseSessionsDoc();
    }
}
