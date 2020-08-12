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
        if (!Response.checkIp(ex)) {
            return;
        }
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
                        ra.connect(vc.getId());
                        MusicResponse aktualnaPiosenka = ra.play(vsc.getAktualnaPiosenka());
                        if (aktualnaPiosenka.isError()) {
                            Log.debug(aktualnaPiosenka.getError().getDescription());
                        }
                        for (String s : vsc.getQueue()) {
                            try {
                                MusicResponse tak = ra.play(s);
                                if (aktualnaPiosenka.isError()) {
                                    Log.debug("2");
                                    Log.debug(tak.getError().getDescription());
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    voiceStateDao.delete(Integer.parseInt(id));
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
