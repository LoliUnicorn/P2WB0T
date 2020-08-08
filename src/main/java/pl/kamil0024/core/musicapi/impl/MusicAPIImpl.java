package pl.kamil0024.core.musicapi.impl;

import lombok.Data;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicRestAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MusicAPIImpl implements MusicAPI {

    private ShardManager api;
    private List<Integer> ports;
    private List<String> clients;
    private HashMap<Integer, String> suma;

    public MusicAPIImpl(ShardManager api) {
        this.api = api;
        this.ports = new ArrayList<>();
        this.suma = new HashMap<>();
        this.clients = new ArrayList<>();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean connect(Integer port) {
        getPorts().remove(port);
        getPorts().add(port);
        String client = getAction(port).clientid();
        Log.debug("client: " + client);
        getClients().add(client);
        getSuma().put(port, client);
        return true;
    }

    @Override
    public boolean disconnect(Integer port) {
        if (!getPorts().contains(port)) {
            return false;
        }
        getPorts().remove(port);
        String client = getClientByPort(port);
        getClients().remove(client);
        getSuma().remove(port);
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

    @Override
    public String getClientByPort(int port) {
        for (Map.Entry<Integer, String> entry : getSuma().entrySet()) {
            if (entry.getKey() == port) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Integer getPortByClient(String client) {
        for (Map.Entry<Integer, String> entry : getSuma().entrySet()) {
            if (entry.getValue().equals(client)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
