package edu.streaming;

import java.util.Map;

/** The example's tiny JSON writer and envelope reader, kept dependency-free. */
final class Json {
    private Json() {}

    static String object(Map<String, Object> values) {
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('"').append(escape(entry.getKey())).append("\":");
            out.append(value(entry.getValue()));
        }
        return out.append('}').toString();
    }

    private static String value(Object value) {
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) out.append(',');
                first = false;
                out.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                out.append(value(entry.getValue()));
            }
            return out.append('}').toString();
        }
        if (value instanceof Iterable<?> values) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (Object item : values) {
                if (!first) out.append(',');
                first = false;
                out.append(value(item));
            }
            return out.append(']').toString();
        }
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        return '"' + escape(String.valueOf(value)) + '"';
    }

    static boolean booleanField(String json, String field) {
        return json.contains("\"" + field + "\":true");
    }

    static String errorCode(String json) {
        String marker = "\"code\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "request_rejected";
        int from = start + marker.length();
        int end = json.indexOf('"', from);
        return end < 0 ? "request_rejected" : json.substring(from, end);
    }

    private static String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
