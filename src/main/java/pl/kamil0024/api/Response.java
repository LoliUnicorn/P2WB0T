package pl.kamil0024.api;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;

public class Response {

    private static final Gson gson = new Gson();

    public static void sendErrorResponse(HttpServerExchange ex, String body, String description) {
        ex.getResponseSender().send(gson.toJson(new ToJSON(false, null, new Error(body, description))));
    }

    public static void sendResponse(HttpServerExchange ex, String msg) {
        ex.getResponseSender().send(gson.toJson(new ToJSON(false, msg, null)));
    }

    @Data
    @AllArgsConstructor
    private static class ToJSON {
        private final boolean success;
        private final String msg;
        private final Error error;
    }

    @Data
    @AllArgsConstructor
    private static class Error {
        private final String body;
        private final String description;
    }

}
