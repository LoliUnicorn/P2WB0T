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

package pl.kamil0024.musicbot.socket;

import com.google.gson.Gson;
import lombok.Data;
import pl.kamil0024.musicbot.core.logger.Log;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class SocketClient extends Thread {

    private final static Gson GSON = new Gson();

    Socket socket;
    OutputStream output;
    PrintWriter writer;

    @Override
    public void start() {

       try {
           while (true) {
               Thread.sleep(10000);

               try {
                   socket = new Socket("localhost", 7070);
                   output = socket.getOutputStream();
                   writer = new PrintWriter(output, true);
                   break;
               } catch (Exception e) {
                   Log.error("Nie udało się połączyć z serwerem!");
                   e.printStackTrace();
               }
           }

           new Thread(() -> {
               try {
                   InputStream input = socket.getInputStream();
                   BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                   String text;
                   while ((text = reader.readLine()) != null) {
                       retrieveMessage(text);
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }).start();

       } catch (Exception e) {
           e.printStackTrace();
       }

    }

    public void retrieveMessage(String msg) {
        Log.debug("Nowa wiadomość od serwera: " + msg);
        Log.debug("Po fromJson otrzymujemy:");
        SocketAction socketAction = GSON.fromJson(msg, SocketAction.class);
        Log.debug("memberId: " + socketAction.getMemberId());
        Log.debug("channelId: " + socketAction.getChannelId());
        Log.debug("topic: " + socketAction.getTopic());
        Log.debug("socketId:" + socketAction.getSocketId());
        Log.debug("args: " + GSON.toJson(socketAction.getArgs()));

        sendMessage("siema, odpowiadam");
    }

    public void sendMessage(String msg) {
        writer.println(msg);
    }

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
