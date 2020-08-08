package pl.kamil0024.core.musicapi;

public interface MusicRestAction {

    MusicResponse testConnection();

    MusicResponse connect(String channelId) throws Exception;
    MusicResponse disconnect() throws Exception;

    String getVoiceChannel();

}
