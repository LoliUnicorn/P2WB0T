package pl.kamil0024.core.musicapi.impl;

import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.NetworkUtil;

public class MusicRestActionImpl implements MusicRestAction {

    private final ShardManager api;
    private final Integer port;

    public MusicRestActionImpl(ShardManager api, Integer port) {
        this.api = api;
        this.port = port;
    }

    @Override
    @Nullable
    public MusicResponse testConnection() {
        try {
            return new MusicResponse(NetworkUtil.getJson(formatUrl("test")));
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void connect(String channelId) throws Exception {
        NetworkUtil.getJson(formatUrl("connect/" + channelId));
    }

    private String formatUrl(String path) {
        return String.format("http://0.0.0.0:%s/api/musicbot/%s", port, path);
    }

}
