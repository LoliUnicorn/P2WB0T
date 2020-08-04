package pl.kamil0024.api.handlers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.config.DiscordInviteConfig;

import java.util.Map;

public class DiscordInvite implements HttpHandler  {

    @Inject private APIModule apiModule;

    public DiscordInvite(APIModule apiModule) {
        this.apiModule = apiModule;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!CheckToken.checkToken(ex)) return;

        String nick = ex.getQueryParameters().get("nick").getFirst();
        String ranga = ex.getQueryParameters().get("ranga").getFirst();
        String kod = ex.getQueryParameters().get("kod").getFirst();

        if (nick.isEmpty() || ranga.isEmpty() || kod.isEmpty()) {
            Response.sendErrorResponse(ex, "Pusty parametr", "Parametr nick,ranga,kod jest pusty.");
            return;
        }

        for (Map.Entry<String, DiscordInviteConfig> dcconfig : apiModule.getDcCache().asMap().entrySet()) {
            if (dcconfig.getValue().getNick().toLowerCase().equals(nick.toLowerCase())) {
                apiModule.getDcCache().invalidate(dcconfig.getKey());
            }
        }

        apiModule.putDiscordConfig(nick, kod, ranga);
        Response.sendResponse(ex, "Zapytanie przebiegło pomyślnie");

    }

}
