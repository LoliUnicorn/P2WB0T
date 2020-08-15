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

package pl.kamil0024.musicbot.api;

import com.google.inject.Inject;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.handlers.*;
import pl.kamil0024.musicbot.api.internale.MiddlewareBuilder;
import pl.kamil0024.musicbot.api.listeners.LeaveVcListener;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.core.module.Modul;
import pl.kamil0024.musicbot.core.redis.RedisManager;
import pl.kamil0024.musicbot.core.util.EventWaiter;
import pl.kamil0024.musicbot.core.util.NetworkUtil;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import java.io.IOException;

import static io.undertow.Handlers.path;

@Getter
public class APIModule implements Modul {

    private ShardManager api;
    private MusicManager musicManager;
    private EventWaiter eventWaiter;
    private boolean start = false;
    Undertow server;

    private final Guild guild;

    private LeaveVcListener leaveVcListener;

    public APIModule(ShardManager api, MusicManager musicManager, EventWaiter eventWaiter) {
        this.api = api;
        this.guild = api.getGuildById(Ustawienia.instance.bot.guildId);
        if (guild == null) throw new UnsupportedOperationException("Gildia docelowa jest nullem!");
        this.musicManager = musicManager;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean startUp() {
        this.leaveVcListener = new LeaveVcListener(musicManager, eventWaiter);
        api.addEventListener(leaveVcListener);
        RoutingHandler routes = new RoutingHandler();

        //#region Main
        routes.get("api/musicbot/test", new CheckToken());
        routes.get("api/musicbot/shutdown", new ShutdownHandler(api));
        routes.get("api/musicbot/clientid", ex -> Response.sendObjectResponse(ex, api.getShards().get(0).getSelfUser().getId()));
        //#endregion Main
        
        //#region VoiceChannel
        routes.get("api/musicbot/connect/{channelid}", new Connect(api));
        routes.get("api/musicbot/disconnect", new Disconnect(api));
        routes.get("api/musicbot/channel", new ChannelHandler(api));
        //#endregion VoiceChannel

        //#region Play
        routes.get("api/musicbot/playlink/{link}", new PlayHandler(api, musicManager));
        routes.get("api/musicbot/skip", new SkipHandler(api, musicManager));
        routes.get("api/musicbot/volume/{liczba}", new VolumeHandler(api, musicManager));
        routes.get("api/musicbot/queue", new QueueHandler(api, musicManager));
        routes.get("api/musicbot/playingtrack", new PlayingTrackHandler(api, musicManager));
        //#endregion Play

        this.server = Undertow.builder()
                .addHttpListener(Ustawienia.instance.api.port, "0.0.0.0")
                .setHandler(path()
                        .addPrefixPath("/", wrapWithMiddleware(routes)))
                .build();
        this.server.start();
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(this.leaveVcListener);
        this.server.stop();
        try {
            NetworkUtil.getJson("http://0.0.0.0:123/api/musicbot/shutdown/" + Ustawienia.instance.api.port);
        } catch (IOException ignored) { }
        return true;
    }

    @Override
    public String getName() {
        return "api";
    }

    @Override
    public boolean isStart() {
        return this.start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

    private static HttpHandler wrapWithMiddleware(HttpHandler handler) {
        return MiddlewareBuilder.begin(BlockingHandler::new).complete(handler);
    }


}
