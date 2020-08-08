package pl.kamil0024.core.musicapi;

import net.dv8tion.jda.api.entities.VoiceChannel;

public interface MusicRestAction {

    MusicResponse testConnection();

    MusicResponse connect(String channelId) throws Exception;
    MusicResponse disconnect() throws Exception;

    VoiceChannel getVoiceChannel();

}
