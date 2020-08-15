/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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

    @Override
    public boolean connect(Integer port, String id) {
        disconnect(port);
        getPorts().add(port);
        getClients().add(id);
        getSuma().put(port, id);
        return true;
    }

    @Override
    public boolean disconnect(Integer port) {
        if (!getPorts().contains(port)) {
            return false;
        }
        String client = getClientByPort(port);
        getPorts().remove(port);
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
