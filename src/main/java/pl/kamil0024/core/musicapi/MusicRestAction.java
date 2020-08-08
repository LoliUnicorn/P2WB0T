package pl.kamil0024.core.musicapi;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import pl.kamil0024.music.commands.QueueCommand;

import java.io.IOException;

@SuppressWarnings("unused")
public interface MusicRestAction {

    MusicResponse testConnection();

    MusicResponse connect(String channelId) throws Exception;
    MusicResponse disconnect() throws Exception;

    VoiceChannel getVoiceChannel();

    MusicResponse shutdown() throws IOException;

    MusicResponse play(String link) throws IOException;

    default MusicResponse play(AudioTrack track) throws IOException {
        return play(track.getIdentifier());
    }

    MusicResponse skip() throws IOException;
    MusicResponse volume(Integer procent) throws IOException;

}
