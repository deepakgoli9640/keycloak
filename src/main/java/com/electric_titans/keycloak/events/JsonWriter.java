package com.electric_titans.keycloak.events;

import java.util.Map;

/** Minimal JSON writer for the controlled event payload shape; avoids shipping a JSON library in a Keycloak provider. */
final class JsonWriter {

    private JsonWriter() {
    }

    static String write(Map<String, ?> value) {
        StringBuilder json = new StringBuilder();
        appendObject(json, value);
        return json.toString();
    }

    private static void appendObject(StringBuilder json, Map<String, ?> object) {
        json.append('{');
        boolean first = true;
        for (Map.Entry<String, ?> entry : object.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            appendString(json, entry.getKey());
            json.append(':');
            appendValue(json, entry.getValue());
        }
        json.append('}');
    }

    private static void appendValue(StringBuilder json, Object value) {
        if (value instanceof Map<?, ?> map) {
            json.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                if (!first) {
                    json.append(',');
                }
                first = false;
                appendString(json, entry.getKey().toString());
                json.append(':');
                appendString(json, entry.getValue().toString());
            }
            json.append('}');
        } else {
            appendString(json, value.toString());
        }
    }

    private static void appendString(StringBuilder json, String value) {
        json.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        json.append('"');
    }
}
