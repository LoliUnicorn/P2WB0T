package pl.kamil0024.core.musicapi;

public interface MusicRestAction {

    MusicResponse testConnection();

    void connect(String channelId) throws Exception;

}
