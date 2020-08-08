package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.music.managers.GuildMusicManager;
import pl.kamil0024.musicbot.music.managers.MusicManager;

public class VolumeHandler implements HttpHandler {

    private final ShardManager api;
    private final MusicManager musicManager;

    public VolumeHandler(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        try {
            Guild guild = Connect.getGuild(api);
            int liczba;
            try {
                liczba = Integer.parseInt(ex.getQueryParameters().get("liczba").getFirst());
                if (liczba <= 0 || liczba > 100) throw new Exception();
            } catch (Exception e) {
                Response.sendErrorResponse(ex, "Błąd", "Zła liczba");
                return;
            }
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }
            GuildMusicManager manager = musicManager.getGuildAudioPlayer(Connect.getGuild(api));

            manager.getPlayer().setVolume(liczba);
            Response.sendResponse(ex, "Pomyślnie zmieniono głośność");

        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd: " + e.getLocalizedMessage());
        }
    }

}
