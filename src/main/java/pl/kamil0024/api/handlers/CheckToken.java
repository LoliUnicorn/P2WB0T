package pl.kamil0024.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;

public class CheckToken implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        String token = ex.getQueryParameters().get("token").getFirst();
        if (token.isEmpty()) {
            Response.sendErrorResponse(ex, "Zły token", "Token jest pusty?");
            return;
        }

        if (!Ustawienia.instance.api.tokens.contains(token)) {
            Response.sendErrorResponse(ex, "Zły token", "Token jest nieprawidłowy.");
            return;
        }

        Response.sendResponse(ex, "Token jest dobry");

    }

}
