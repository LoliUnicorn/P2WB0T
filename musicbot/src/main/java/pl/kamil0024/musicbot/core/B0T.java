package pl.kamil0024.musicbot.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.api.APIModule;
import pl.kamil0024.musicbot.core.listener.ShutdownListener;
import pl.kamil0024.musicbot.core.logger.Log;
import pl.kamil0024.musicbot.core.module.Modul;
import pl.kamil0024.musicbot.core.module.ModulManager;
import pl.kamil0024.musicbot.core.redis.RedisManager;
import pl.kamil0024.musicbot.core.util.EventWaiter;
import pl.kamil0024.musicbot.core.util.NetworkUtil;
import pl.kamil0024.musicbot.core.util.Statyczne;
import pl.kamil0024.musicbot.core.util.Tlumaczenia;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class B0T {

    @Getter private HashMap<String, Modul> moduls;

    private static final File cfg = new File("config.json");

    private Ustawienia ustawienia;
    private Tlumaczenia tlumaczenia;
    private Thread shutdownThread;

    private ShardManager api;
    private ModulManager modulManager;

    public B0T(String token) {
        moduls = new HashMap<>();
        tlumaczenia = new Tlumaczenia();
        shutdownThread();

        Log.info("Loguje v%s", Statyczne.CORE_VERSION);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        if (!cfg.exists()) {
            try {
                if (cfg.createNewFile()) {
                    ustawienia = new Ustawienia();
                    Files.write(cfg.toPath(), gson.toJson(ustawienia).getBytes(StandardCharsets.UTF_8));
                    Log.info("Konfiguracja stworzona, mozesz ustawic bota");
                    System.exit(1);
                }
            } catch (Exception e) {
                Log.error("Nie udalo sie stworzyc konfiguracji! %s", e);
                System.exit(1);
            }
        }

        try {
            ustawienia = gson.fromJson(new FileReader(cfg), Ustawienia.class);
        } catch (Exception e) {
            Log.error("Nie mozna odczytac konfiguracji\n%s", e);
            System.exit(1);
        }

        Ustawienia.instance = ustawienia;
        EventWaiter eventWaiter;
        try {
            eventWaiter = new EventWaiter();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return; // super jestes idea
        }

        tlumaczenia.setLang("pl");
        tlumaczenia.load();

        this.api = null;
        try {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES);
            builder.setShardsTotal(1);
            builder.setShards(0, 0);
            builder.setEnableShutdownHook(false);
            builder.setAutoReconnect(true);
            builder.setStatus(OnlineStatus.OFFLINE);
            builder.addEventListeners(eventWaiter, new ShutdownListener());
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setCallbackPool(Executors.newFixedThreadPool(4));
            this.api = builder.build();
            Thread.sleep(1000);
            while (api.getShards().stream().noneMatch(s -> {
                try {
                    s.getSelfUser();
                } catch (IllegalStateException e) {
                    return false;
                }
                return true;
            })) {
                Thread.sleep(600);
            }
        } catch (LoginException | InterruptedException e) {
            Log.error("Nie udalo sie zalogowac!");
            e.printStackTrace();
            System.exit(1);
        }

        Optional<JDA> shard = api.getShards().stream().filter(s -> {
            try {
                s.getSelfUser();
            } catch (IllegalStateException e) {
                return false;
            }
            return true;
        }).findAny();

        try {
            Thread.sleep(8000);
        } catch (InterruptedException ignored) {}

        RedisManager     redisManager        = new RedisManager(shard.get().getSelfUser().getIdLong());

        this.modulManager = new ModulManager();

        APIModule apiModule = new APIModule(api, redisManager);

        modulManager.getModules().add(apiModule);

        for (Modul modul : modulManager.getModules()) {
            try {
                Log.debug(tlumaczenia.get("module.loading", modul.getName()));
                boolean bol = false;
                try {
                    bol = modul.startUp();
                    modul.setStart(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!bol)
                    Log.error(tlumaczenia.get("module.loading.fail"));
                else
                    Log.debug(tlumaczenia.get("module.loading.success", modul.getName()));

                moduls.put(modul.getName(), modul);
            } catch (Exception ignored) {
                Log.error(tlumaczenia.get("module.loading.fail"));
            }
        }


        api.setStatus(OnlineStatus.OFFLINE);
        Log.info("Zalogowano jako %s", shard.get().getSelfUser().getName());

        if (api.getGuildById(Ustawienia.instance.bot.guildId) == null) {
            api.shutdown();
            Log.newError("Nie ma bota na serwerze docelowym");
            System.exit(1);
            return;
        }

        try {
            NetworkUtil.getJson(String.format("http://0.0.0.0:%s/api/musicbot/connect/%s", Ustawienia.instance.api.mainPort, Ustawienia.instance.api.port));
        } catch (Exception e) {
            Log.newError("Nie udało się podłączyć do głównego api");
            Log.newError(e);
        }

    }

    public void shutdownThread() {
        this.shutdownThread = new Thread(() -> {
            Log.info("Zamykam...");
            try {
                NetworkUtil.getJson("http://0.0.0.0:123/api/musicbot/shutdown/" + Ustawienia.instance.api.port);
            } catch (Exception ignored) {}
            modulManager.disableAll();
            api.shutdown();
        });
        shutdownThread.setName("P2WB0T ShutDown");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }


}
