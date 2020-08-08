package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.core.logger.Log;
import pl.kamil0024.musicbot.music.managers.MusicManager;

public class PlayHandler implements HttpHandler {

    private final ShardManager api;
    private final MusicManager musicManager;

    public PlayHandler(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        try {
            String track = ex.getQueryParameters().get("link").getFirst();
            if (track.isEmpty()) {
                Response.sendErrorResponse(ex, "Zły parametr", "Parametr {link} jest pusty");
                return;
            }
            Log.debug("track:" + track);
            track = "https://www.youtube.com/watch?v=" + track;
            Log.debug("trackv2:" + track);

            Guild guild = Connect.getGuild(api);
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }
            if (musicManager.loadAndPlay(guild, track, state.getConnectedChannel())) {
                Response.sendResponse(ex, "Pomyślnie dodano piosenkę do kolejki");
                return;
            }
            Response.sendErrorResponse(ex, "Nie udało się odtworzyć piosenki!", "Link jest nieprawidłowy!");

        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd: " + e.getLocalizedMessage());
        }

    }

}
