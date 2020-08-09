package pl.kamil0024.musicbot.api.handlers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.music.managers.MusicManager;

public class PlayingTrackHandler implements HttpHandler {

    private ShardManager api;
    private MusicManager musicManager;

    public PlayingTrackHandler(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        try {
            AudioTrack track = musicManager.getGuildAudioPlayer(Connect.getGuild(api)).getPlayer().getPlayingTrack();
            if (track == null) {
                Response.sendErrorResponse(ex, "Nic nie leci", "Bot nic nie gra");
                return;
            }
            Response.sendObjectResponse(ex, new QueueHandler.Track(track));
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd: " + e.getLocalizedMessage());
        }
        Response.sendObjectResponse(ex,
                new QueueHandler.Track(musicManager
                        .getGuildAudioPlayer(Connect.getGuild(api)).getPlayer().getPlayingTrack()));
    }

}
