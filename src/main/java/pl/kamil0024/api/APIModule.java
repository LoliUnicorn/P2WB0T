package pl.kamil0024.api;

import com.google.gson.Gson;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.handlers.CheckToken;
import pl.kamil0024.api.internale.MiddlewareBuilder;
import pl.kamil0024.core.module.Modul;

import static io.undertow.Handlers.path;

public class APIModule implements Modul {

    private ShardManager api;
    private boolean start = false;
    Undertow server;

    public APIModule(ShardManager api) {
        this.api = api;
    }

    @Override
    public boolean startUp() {
        Gson gson = new Gson();
        RoutingHandler routes = new RoutingHandler();

        routes.get("/api/checkToken/{token}", new CheckToken());

        this.server = Undertow.builder()
                .addHttpListener(1234, "0.0.0.0")
                .setHandler(path()
                        .addPrefixPath("/", wrapWithMiddleware(routes)))
                .build();

        return true;
    }

    @Override
    public boolean shutDown() {
        this.server.stop();
        return true;
    }

    @Override
    public String getName() {
        return "API";
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
