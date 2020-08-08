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
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.core.module.Modul;
import pl.kamil0024.musicbot.core.redis.RedisManager;
import pl.kamil0024.musicbot.core.util.NetworkUtil;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import java.io.IOException;

import static io.undertow.Handlers.path;

@Getter
public class APIModule implements Modul {

    private ShardManager api;
    private MusicManager musicManager;
    private boolean start = false;
    Undertow server;

    @Inject private RedisManager redisManager;

    private final Guild guild;

    public APIModule(ShardManager api, RedisManager redisManager, MusicManager musicManager) {
        this.api = api;
        this.redisManager = redisManager;
        this.guild = api.getGuildById(Ustawienia.instance.bot.guildId);
        if (guild == null) throw new UnsupportedOperationException("Gildia docelowa jest nullem!");
        this.musicManager = musicManager;
    }

    @Override
    public boolean startUp() {
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
