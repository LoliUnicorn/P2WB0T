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
import lombok.Data;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.socket.actions.*;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SocketManager {

    @Getter private final HashMap<Integer, SocketClient> clients;

    private final ShardManager api;

    public SocketManager(AsyncEventBus eventBus, ShardManager api) {
        clients = new HashMap<>();
        eventBus.register(this);
        this.api = api;
    }

    @Subscribe
    public void retrieveMessage(SocketServer.SocketJson socketJson) {
        Log.debug("Nowa wiadomość socketa %s:", socketJson.getId());
        Log.debug(socketJson.getJson());
        if (socketJson.getJson().startsWith("setBotId:")) {
            String id = socketJson.getJson().split("setBotId:")[1];
            clients.get(socketJson.getId()).setBotId(id);
            return;
        }

        try {
            Response response = SocketServer.GSON.fromJson(socketJson.getJson(), Response.class);
            TextChannel txt = api.getTextChannelById(response.getAction().getChannelId());
            if (txt == null) throw new NullPointerException("Kanal jakims cudem jest nullem");
            String ping = String.format("<@%s>", response.getAction().getMemberId());
            if (!response.isSuccess()) {
                Message msg = txt.sendMessage(ping + ", " + response.getErrorMessage()).complete();
                msg.addReaction(CommandExecute.getReaction(msg.getAuthor(), false)).queue();
                return;
            }

            if (!response.isSendMessage()) return;

            if (response.getMessageType().equals("message")) {
                if (response.getData() == null) return;
                Message msg = txt.sendMessage(ping + ", " + response.getData()).complete();
                msg.addReaction(CommandExecute.getReaction(msg.getAuthor(), true)).queue();
            }

        } catch (Exception e) {
            Log.newError("Socket client wyslal zla wiadomosc! W logach masz wiadomosc", getClass());
            Log.error(socketJson.getJson());
            Log.newError(e, getClass());
        }

    }

    public void sendMessage(SocketAction socketAction) {
        SocketClient client = clients.get(socketAction.getSocketId());
        if (client == null) {
            Log.newError("Próbowano wysłać wiadomość do socketa %s, ale ten nie istnieje!", getClass(), socketAction.getSocketId());
            return;
        }
        client.getWriter().println(socketAction.toJson());
    }

    @Subscribe
    public void disconnectEvent(SocketServer.SocketDisconnect socketDisconnect) {
        Log.debug("Odłączono socketa %s", socketDisconnect.getId());
        clients.remove(socketDisconnect.getId());
    }

    public Action getAction(String memberId, String channelId, int socketId) {
        return new Action(this, memberId, channelId, socketId, true);
    }

    @AllArgsConstructor
    public static class Action {
        private final SocketManager manager;
        private final String memberId;
        private final String channelId;
        private final int socketId;

        private boolean sendMessage;

        public void connect(String voiceChannelId) {
            manager.sendMessage(new ConnectAction(memberId, channelId, socketId, voiceChannelId));
        }
        public void disconnect() {
            manager.sendMessage(new DisconnectAction(memberId, channelId, socketId));
        }
        public void play(String track) {
            manager.sendMessage(new PlayAction(memberId, channelId, socketId, track));
        }
        public void queue() {
            manager.sendMessage(new QueueAction(memberId, channelId, socketId));
        }
        public void shutdown() {
            manager.sendMessage(new ShutdownAction(memberId, channelId, socketId));
        }
        public void skip() {
            manager.sendMessage(new SkipAction(memberId, channelId, socketId));
        }
        public void volume(int procent) {
            manager.sendMessage(new VolumeAction(memberId, channelId, socketId, procent));
        }

        public Action setSendMessage(boolean bol) {
            sendMessage = bol;
            return this;
        }

        public boolean getSendMessage() {
            return this.sendMessage;
        }

    }

    @Data
    @AllArgsConstructor
    public static class Response {
        private final SocketAction action;
        private final boolean success;
        private final boolean sendMessage;
        private final String errorMessage;
        private final String messageType;
        private final Object data;

        @Data
        public static class SocketAction {
            public SocketAction() { }

            private String memberId;
            private String channelId;
            private String topic;
            private int socketId;
            private Map<String, Object> args;

        }

    }

}
