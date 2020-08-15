/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.musicbot.api;

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
