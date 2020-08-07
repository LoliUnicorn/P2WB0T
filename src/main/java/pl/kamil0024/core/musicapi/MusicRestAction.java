package pl.kamil0024.core.musicapi;

import pl.kamil0024.core.util.JSONResponse;

public interface MusicRestAction {

    MusicResponse testConnection();

    JSONResponse connect(String channelId) throws Exception;
    JSONResponse disconnect() throws Exception;

}
