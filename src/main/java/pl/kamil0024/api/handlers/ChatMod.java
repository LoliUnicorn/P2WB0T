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

package pl.kamil0024.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.logger.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatMod implements HttpHandler {

    private ShardManager api;
    private APIModule apiModule;

    public ChatMod(ShardManager api, APIModule apiModule) {
        this.api = api;
        this.apiModule = apiModule;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!CheckToken.checkToken(ex)) return;

        Guild guild = api.getGuildById(Ustawienia.instance.bot.guildId);
        if (guild == null) {
            Log.newError("Brak serwera docelowego", ChatMod.class);
            Response.sendErrorResponse(ex, "Błąd", "Brak serwera docelowego");
            return;
        }
        List<APIModule.ChatModUser> chatmod = new ArrayList<>();
        for (Map.Entry<String, APIModule.ChatModUser> entry : apiModule.getChatModUsers().entrySet()) {
            chatmod.add(entry.getValue());
        }
        Response.sendObjectResponse(ex, chatmod);
    }
}
