package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;

public class ChannelHandler implements HttpHandler {

    private final ShardManager api;

    public ChannelHandler(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {

        try {
            Guild guild = Connect.getGuild(api);
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }
            Response.sendObjectResponse(ex, state.getConnectedChannel().getId());
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd");
        }

    }

}
