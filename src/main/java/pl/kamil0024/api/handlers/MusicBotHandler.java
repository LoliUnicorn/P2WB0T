package pl.kamil0024.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.logger.Log;
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
        Log.debug(ex.getSourceAddress().getAddress().getHostAddress());
        Response.sendResponse(ex, "Tak");
    }


}
