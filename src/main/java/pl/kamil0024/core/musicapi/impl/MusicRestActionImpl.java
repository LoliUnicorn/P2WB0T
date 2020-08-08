package pl.kamil0024.core.musicapi.impl;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.NetworkUtil;

@SuppressWarnings("ConstantConditions")
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
    public MusicResponse connect(String channelId) throws Exception {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("connect/" + channelId)));
    }

    @Override
    public MusicResponse disconnect() throws Exception {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("disconnect")));
    }

    @Override
    @Nullable
    public VoiceChannel getVoiceChannel() throws Exception {
        try {
            MusicResponse mr = new MusicResponse(NetworkUtil.getJson(formatUrl("channel")));
            String id = mr.json.getString("data");
            Log.debug(id);
            return api.getVoiceChannelById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatUrl(String path) {
        return String.format("http://0.0.0.0:%s/api/musicbot/%s", port, path);
    }

}
