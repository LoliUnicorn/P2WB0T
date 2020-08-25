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

package pl.kamil0024.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.logger.Log;

import java.awt.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class YouTrackReport implements HttpHandler {

    private final ShardManager api;

    public YouTrackReport(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        Response.sendResponse(ex, "Pomyślnie wysłano reporta!");
        String header = ex.getRequestHeaders().get("data").getFirst();

        Type typeOfHashMap = new TypeToken<Map<String, Integer>>() { }.getType();
        HashMap<String, Integer> map = new Gson().fromJson(header, typeOfHashMap);
        TextChannel txt = api.getTextChannelById("738122215878295572");

        MessageBuilder mb = new MessageBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.**MM**");
        mb.setContent("Statystyki z " + sdf.format(new Date()));
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.red);
        eb.setTimestamp(Instant.now());

        int miejsce = 1;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append(miejsce).append(". ").append(entry.getKey()).append(": ").append(entry.getValue());
            miejsce++;
        }
        eb.addField("Stworzonych issuesów", sb.toString(), false);

        Objects.requireNonNull(txt).sendMessage(mb.build()).queue();
    }

}
