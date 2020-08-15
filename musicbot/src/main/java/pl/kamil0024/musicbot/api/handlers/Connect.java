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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.Response;
import pl.kamil0024.musicbot.core.Ustawienia;

public class Connect implements HttpHandler {

    private final ShardManager api;

    public Connect(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        String channelid = ex.getQueryParameters().get("channelid").getFirst();
        if (channelid.isEmpty()) {
            Response.sendErrorResponse(ex, "Złe ID", "ID kanału jest puste?");
            return;
        }
        VoiceChannel vc = api.getVoiceChannelById(channelid);
        if (vc == null) {
            Response.sendErrorResponse(ex, "Złe ID", "Nie ma kanału o takim ID");
            return;
        }

        try {
            Guild guild = getGuild(api);
            guild.getAudioManager().openAudioConnection(vc);
            Response.sendResponse(ex, "Bot dołączył na kanał głosowy");
        } catch (InsufficientPermissionException e) {
            Response.sendErrorResponse(ex, "Brak permisji", "Bot nie ma wystarczających permisji");
        } catch (UnsupportedOperationException e) {
            Response.sendErrorResponse(ex, "Błąd JDA", "Wystąpił wewnętrzny błąd z JDA:" + e.getLocalizedMessage());
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Wystąpił błąd:" + e.getLocalizedMessage());
        }

    }

    public static Guild getGuild(ShardManager api) {
        return api.getGuildById(Ustawienia.instance.bot.guildId);
    }

}
