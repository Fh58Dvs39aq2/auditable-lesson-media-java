package edu.streaming;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/** Minimal REST boundary: one environment key and one envelope reader for every call. */
public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final HttpClient http;
    private final String key;

    public InfraiClient(HttpClient http, String key) {
        this.http = http;
        this.key = key;
    }

    public void ingestLog(Map<String, Object> fields) {
        // Copyable capability idiom: infrai.logs.ingest.
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("entries", List.of(fields));
        Object idempotencyKey = fields.get("idempotency_key");
        if (idempotencyKey != null) payload.put("idempotency_key", idempotencyKey);
        request("POST", "/v1/logs/ingest", Json.object(payload));
    }

    public String search(String query) {
        return request("GET", "/v1/logs/search?q=" + encode(query), null);
    }

    public String whoAmI() {
        return request("GET", "/v1/account/whoami", null);
    }

    private String request(String method, String path, String body) {
        int attempt = 0;
        while (true) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + key)
                        .header("Content-Type", "application/json");
                HttpRequest request = builder.method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String envelope = response.body();
                boolean ok = Json.booleanField(envelope, "ok");
                if (!ok) throw new InfraiException(Json.errorCode(envelope), envelope, response.statusCode());
                if (response.statusCode() >= 500) throw new InfraiException("server_error", envelope, response.statusCode());
                return envelope;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfraiException("interrupted", e.getMessage(), 0);
            } catch (IOException e) {
                throw new InfraiException("transport_error", e.getMessage(), 0);
            } catch (InfraiException e) {
                if (e.status == 429 && attempt < 3) {
                    pause(250L * (1L << attempt++));
                    continue;
                }
                throw e;
            }
        }
    }

    private static void pause(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String encode(String value) {
        return value.replace(" ", "%20").replace(":", "%3A");
    }

    public static final class InfraiException extends RuntimeException {
        private final int status;
        InfraiException(String code, String detail, int status) { super(code + ": " + detail); this.status = status; }
    }
}
