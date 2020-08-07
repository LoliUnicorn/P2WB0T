package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.api.Response;

public class CheckToken implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        Response.sendResponse(ex, "Połączenie jest dobre!");
    }

}
