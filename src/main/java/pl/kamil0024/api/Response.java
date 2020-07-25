package pl.kamil0024.api;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.charset.StandardCharsets;

public class Response {

    private static final Gson gson = new Gson();

    public static void sendErrorResponse(HttpServerExchange ex, String body, String description) {
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        ex.getResponseSender().send(gson.toJson(new ToJSON(false, null, new Error(body, description), null)), StandardCharsets.UTF_8);
    }

    public static void sendResponse(HttpServerExchange ex, String msg) {
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        ex.getResponseSender().send(gson.toJson(new ToJSON(true, msg, null, null)), StandardCharsets.UTF_8);
    }

    public static void sendObjectResponse(HttpServerExchange ex, Object data) {
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        ex.getResponseSender().send(gson.toJson(new ToJSON(true, null, null, data)), StandardCharsets.UTF_8);
    }

    @Data
    @AllArgsConstructor
    private static class ToJSON {
        private final boolean success;
        private final String msg;
        private final Error error;
        private final Object data;
    }

    @Data
    @AllArgsConstructor
    private static class Error {
        private final String body;
        private final String description;
    }

}
