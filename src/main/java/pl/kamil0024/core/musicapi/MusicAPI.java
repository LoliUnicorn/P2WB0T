package pl.kamil0024.core.musicapi;

import java.util.List;

public interface MusicAPI {

    boolean connect(Integer port);
    boolean disconnect(Integer port);

    void stop(int port);

    MusicRestAction getAction(Integer port);

    List<Integer> getPorts();

    List<String> getClients();

    String getClientByPort(int port);

    Integer getPortByClient(String client);

}
