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
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.socket.actions.ConnectAction;
import pl.kamil0024.core.socket.actions.DisconnectAction;
import pl.kamil0024.core.socket.actions.SocketAction;

import java.util.HashMap;

public class SocketManager {

    @Getter private final HashMap<Integer, SocketClient> clients;

    public SocketManager(AsyncEventBus eventBus) {
        clients = new HashMap<>();
        eventBus.register(this);
    }

    @Subscribe
    public void retrieveMessage(SocketServer.SocketJson socketJson) {
        Log.debug("Nowa wiadomość socketa %s:", socketJson.getId());
        Log.debug(socketJson.getJson());
    }

    public void sendMessage(SocketAction socketAction) {
        SocketClient client = clients.get(socketAction.getSocketId());
        if (client == null) {
            Log.error("Próbowano wysłać wiadomość do socketa %s, ale ten nie istnieje!", socketAction.getSocketId());
            return;
        }
        client.getWriter().write(socketAction.toJson());
    }

    @Subscribe
    public void disconnectEvent(SocketServer.SocketDisconnect socketDisconnect) {
        Log.debug("Odłączono socketa %s", socketDisconnect.getId());
        clients.remove(socketDisconnect.getId());
    }

    public Action getAction(String memberId, String channelId, int socketId) {
        return new Action(this, memberId, channelId, socketId);
    }

    @AllArgsConstructor
    public static class Action {
        private final SocketManager manager;
        private final String memberId;
        private final String channelId;
        private final int socketId;

        public void connect(String voiceChannelId) {
            manager.sendMessage(new ConnectAction(memberId, channelId, socketId, voiceChannelId));
        }

        public void disconnect() {
            manager.sendMessage(new DisconnectAction(memberId, channelId, socketId));
        }

    }


}
