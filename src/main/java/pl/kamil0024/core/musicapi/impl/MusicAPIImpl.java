package pl.kamil0024.core.musicapi.impl;

import lombok.Data;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicRestAction;

import java.util.ArrayList;
import java.util.List;

@Data
public class MusicAPIImpl implements MusicAPI {

    private ShardManager api;
    private List<Integer> ports;

    public MusicAPIImpl(ShardManager api) {
        this.api = api;
        this.ports = new ArrayList<>();
    }

    @Override
    public boolean connect(Integer port) {
        if (getPorts().contains(port)) {
            return false;
        }
        getPorts().add(port);
        return true;
    }

    @Override
    public boolean disconnect(Integer port) {
        if (!getPorts().contains(port)) {
            return false;
        }
        getPorts().remove(port);
        return true;
    }

    @Override
    public void stop(int port) {
        getPorts().forEach(this::disconnect);
    }

    @Override
    @Nullable
    public MusicRestAction getAction(Integer ind) {
        if (!getPorts().contains(ind)) {
            return null;
        }
        return new MusicRestActionImpl(getApi(), ind);
    }

}
