package pl.kamil0024.core.musicapi;

public interface MusicAPI {

    boolean connect(Integer port);
    boolean disconnect(Integer port);

    void stop(int port);

    MusicRestAction getAction(Integer port);

}
