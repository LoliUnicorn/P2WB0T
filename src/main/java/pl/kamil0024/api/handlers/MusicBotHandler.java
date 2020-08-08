package pl.kamil0024.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.musicapi.MusicAPI;

public class MusicBotHandler implements HttpHandler {

    private final boolean connect;
    private final MusicAPI musicAPI;

    public MusicBotHandler(MusicAPI musicAPI, boolean connect) {
        this.connect = connect;
        this.musicAPI = musicAPI;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!Response.checkIp(ex)) {
            return;
        }
        try {
            Integer port = Integer.valueOf(ex.getQueryParameters().get("port").getFirst());
            String id = ex.getQueryParameters().get("clientid").getFirst();
            if (connect) musicAPI.connect(port, id);
            else musicAPI.disconnect(port);
            Response.sendResponse(ex, "Pomyślnie zarejestrowano port");
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Zły port", "Port nie jest liczbą!");
        }
    }


}
