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

package pl.kamil0024.musicbot.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;

public class Disconnect implements HttpHandler {

    private final ShardManager api;

    public Disconnect(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        try {
            Guild guild = Connect.getGuild(api);
            AudioManager state = guild.getAudioManager();
            if (state.getConnectedChannel() == null) {
                Response.sendErrorResponse(ex, "Błąd", "Bot nie jest na żadnym kanale!");
                return;
            }
            state.closeAudioConnection();
            Response.sendResponse(ex, "Pod opuścił kanał głosowy");
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd");
        }

    }

}
