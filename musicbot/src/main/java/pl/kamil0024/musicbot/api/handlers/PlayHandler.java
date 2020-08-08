package pl.kamil0024.musicbot.api.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.music.managers.GuildMusicManager;
import pl.kamil0024.musicbot.music.managers.MusicManager;

@SuppressWarnings("DuplicatedCode")
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
            track = "https://www.youtube.com/watch?v=" + track;

            Guild guild = Connect.getGuild(api);
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }

            GuildMusicManager serwerManager = musicManager.getGuildAudioPlayer(Connect.getGuild(api));

            serwerManager.getManager().loadItemOrdered(serwerManager, track, new AudioLoadResultHandler() {

                @Override
                public void trackLoaded(AudioTrack track) {
                    musicManager.play(Connect.getGuild(api), serwerManager, track, state.getConnectedChannel());
                    Response.sendResponse(ex, "Pomyślnie dodano piosenkę do kolejki");
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for (AudioTrack track : playlist.getTracks()) {
                        musicManager.play(Connect.getGuild(api), serwerManager, track, state.getConnectedChannel());
                    }
                    Response.sendResponse(ex, "Pomyślnie dodano piosenkę do kolejki");
                }

                @Override
                public void noMatches() {
                    Response.sendErrorResponse(ex, "Nie udało się odtworzyć piosenki!", "Link jest nieprawidłowy!");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    Response.sendErrorResponse(ex, "Nie udało się odtworzyć piosenki!", "Link jest nieprawidłowy!");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd: " + e.getLocalizedMessage());
        }

    }

}
