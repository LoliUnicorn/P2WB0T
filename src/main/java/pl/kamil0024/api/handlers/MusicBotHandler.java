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
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.VoiceStateDao;
import pl.kamil0024.core.database.config.VoiceStateConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;

import java.io.IOException;

public class MusicBotHandler implements HttpHandler {

    private final boolean connect;
    private final MusicAPI musicAPI;
    private final VoiceStateDao voiceStateDao;
    private final ShardManager shardManager;

    public MusicBotHandler(MusicAPI musicAPI, boolean connect, VoiceStateDao voiceStateDao, ShardManager shardManager) {
        this.connect = connect;
        this.musicAPI = musicAPI;
        this.voiceStateDao = voiceStateDao;
        this.shardManager = shardManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!Response.checkIp(ex)) { return; }
        try {
            Integer port = Integer.valueOf(ex.getQueryParameters().get("port").getFirst());
            String id = ex.getQueryParameters().get("clientid").getFirst();
            if (connect) {
                musicAPI.connect(port, id);
                VoiceStateConfig vsc = voiceStateDao.get(id);
                if (vsc != null && vsc.getVoiceChannel() != null) {
                    VoiceChannel vc = shardManager.getVoiceChannelById(vsc.getVoiceChannel());
                    if (vc != null) {
                        MusicRestAction ra = musicAPI.getAction(port);
                        MusicResponse kurwa = ra.connect(vc.getId());
                        Thread.sleep(4000);
                        ra.play(vsc.getAktualnaPiosenka());
                        for (String s : vsc.getQueue()) {
                            try {
                                ra.play(s);
                            } catch (Exception ignored) {}
                        }
                    }
                    voiceStateDao.delete(id);
                }
            } else {
                musicAPI.disconnect(port);
            }

            Response.sendResponse(ex, "Pomyślnie zarejestrowano port");
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Zły port", "Port nie jest liczbą!");
        }
    }


}
