package pl.kamil0024.musicbot.api;

import com.google.inject.Inject;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.handlers.CheckToken;
import pl.kamil0024.musicbot.api.handlers.Connect;
import pl.kamil0024.musicbot.api.handlers.Disconnect;
import pl.kamil0024.musicbot.api.internale.MiddlewareBuilder;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.core.module.Modul;
import pl.kamil0024.musicbot.core.redis.RedisManager;
import pl.kamil0024.musicbot.core.util.NetworkUtil;

import java.io.IOException;

import static io.undertow.Handlers.path;

@Getter
public class APIModule implements Modul {

    private ShardManager api;
    private boolean start = false;
    Undertow server;

    @Inject private RedisManager redisManager;

    private final Guild guild;

    public APIModule(ShardManager api, RedisManager redisManager) {
        this.api = api;
        this.redisManager = redisManager;
        this.guild = api.getGuildById(Ustawienia.instance.bot.guildId);
        if (guild == null) throw new UnsupportedOperationException("Gildia docelowa jest nullem!");
    }

    @Override
    public boolean startUp() {
        RoutingHandler routes = new RoutingHandler();

        routes.get("api/musicbot/test", new CheckToken());
        routes.get("api/musicbot/connect/{channelid}", new Connect(api));
        routes.get("api/musicbot/disconnect", new Disconnect(api));

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
