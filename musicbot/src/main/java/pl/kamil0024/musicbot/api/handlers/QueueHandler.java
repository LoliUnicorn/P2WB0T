package pl.kamil0024.musicbot.api.handlers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.Data;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class QueueHandler implements HttpHandler {

    public ShardManager api;
    public MusicManager musicManager;

    public QueueHandler(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }


    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        List<AudioTrack> klele = new ArrayList<>(musicManager.getGuildAudioPlayer(Connect.getGuild(api)).getQueue());
        
        if (klele.isEmpty()) {
            Response.sendErrorResponse(ex, "Błąd", "Kolejka jest pusta!");
            return;
        }
        List<Track> traki = new ArrayList<>();
        klele.forEach(t -> traki.add(new Track(t)));
        Response.sendObjectResponse(ex, traki);
    }
    
    @Data
    public static class Track {
        
        private final String identifier;
        private final String author;
        private final String title;
        private final boolean stream;
        private final long lenght;

        public Track(AudioTrack audioTrack) {
            this.identifier = audioTrack.getIdentifier();
            this.author = audioTrack.getInfo().author;
            this.title = audioTrack.getInfo().title;
            this.stream = audioTrack.getInfo().isStream;
            this.lenght = audioTrack.getInfo().length;
        }
        
    }

}
