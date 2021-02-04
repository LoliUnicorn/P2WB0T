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

package pl.kamil0024.core.socket;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.kamil0024.core.logger.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class SocketServer extends Thread {

    private final static Gson GSON = new Gson();

    private final HashMap<Integer, SocketClient> clients;
    AsyncEventBus eventBus;

    public SocketServer(AsyncEventBus eventBus) {
        clients = new HashMap<>();
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Override
    public void start() {

        ServerSocket serverSocket;
        Socket socket;

        try {
            serverSocket = new ServerSocket(7070);
        } catch (Exception e) {
            Log.newError("Nie udało się wystartować servera z socketem", getClass());
            Log.newError(e, getClass());
            return;
        }

        while (true) {
            try {
                socket = serverSocket.accept();
                SocketClient client = new SocketClient(socket, eventBus);
                client.start();
                clients.put(client.getSocketId(), client);
                Log.debug("Podłączono nowy socket!");
            } catch (Exception e) {
                Log.newError(e, getClass());
            }

        }

    }


    @Subscribe
    public void retrieveMessage(SocketJson socketJson) {
        Log.debug("Nowa wiadomość socketa %s:", socketJson.getId());
        Log.debug(socketJson.getJson());
    }

    public void sendMessage(SocketJson socketJson) {
        SocketClient client = clients.get(socketJson.getId());
        if (client == null) {
            Log.error("Próbowano wysłać wiadomość do socketa %s, ale ten nie istnieje!", socketJson.getId());
            return;
        }
        client.getWriter().write(GSON.toJson(socketJson));
    }

    @Subscribe
    public void disconnectEvent(SocketDisconnect socketDisconnect) {
        Log.debug("Odłączono socketa %s", socketDisconnect.getId());
        clients.remove(socketDisconnect.getId());
    }

    @Data
    @AllArgsConstructor
    public static class SocketJson {
        private final int id;
        private final String json;
    }

    @Data
    @AllArgsConstructor
    public static class SocketDisconnect {
        private final int id;
    }

}
