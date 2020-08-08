package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.music.managers.GuildMusicManager;
import pl.kamil0024.musicbot.music.managers.MusicManager;

public class SkipHandler implements HttpHandler {

    private final ShardManager api;
    private final MusicManager musicManager;

    public SkipHandler(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        try {
            Guild guild = Connect.getGuild(api);
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }
            GuildMusicManager manager = musicManager.getGuildAudioPlayer(Connect.getGuild(api));
            if (manager.getPlayer().getPlayingTrack() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot aktualnie nic nie gra?");
                return;
            }
            manager.nextTrack();
            Response.sendResponse(ex, "Puszczono następną piosenkę");

        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd: " + e.getLocalizedMessage());
        }
    }

}
