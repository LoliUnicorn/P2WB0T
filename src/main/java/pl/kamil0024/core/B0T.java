package pl.kamil0024.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.chat.ChatModule;
import pl.kamil0024.commands.CommandsModule;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.dews.RebootCommand;
import pl.kamil0024.core.arguments.ArgumentManager;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.database.*;
import pl.kamil0024.core.listener.ExceptionListener;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.redis.RedisManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.Statyczne;
import pl.kamil0024.core.util.Tlumaczenia;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.liczydlo.LiczydloModule;
import pl.kamil0024.logs.LogsModule;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.nieobecnosci.NieobecnosciManager;
import pl.kamil0024.nieobecnosci.NieobecnosciModule;
import pl.kamil0024.stats.StatsModule;
import pl.kamil0024.weryfikacja.WeryfikacjaModule;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;

import static pl.kamil0024.core.util.Statyczne.WERSJA;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class B0T {

    @Getter private HashMap<String, Modul> moduls;

    @Inject private CommandManager commandManager;
    @Inject private ArgumentManager argumentManager;
    @Inject private DatabaseManager databaseManager;

    private static final File cfg = new File("config.json");

    private Ustawienia ustawienia;
    private Tlumaczenia tlumaczenia;
    private Thread shutdownThread;

    private StatsModule statsModule;
    private ShardManager api;
    private ModulManager modulManager;
    private MusicModule musicModule;

    public B0T(String token) {
        moduls = new HashMap<>();
        tlumaczenia = new Tlumaczenia();
        argumentManager = new ArgumentManager();
        commandManager = new CommandManager();

        argumentManager.registerAll();
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
        KaryJSON karyJSON;
        try {
            eventWaiter = new EventWaiter();
            karyJSON = new KaryJSON();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return; // super jestes idea
        }

        tlumaczenia.setLang(Ustawienia.instance.language);
        tlumaczenia.load();

        this.api = null;
        try {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token,
                    GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_PRESENCES);
            builder.setShardsTotal(1);
            builder.setShards(0, 0);
            builder.setEnableShutdownHook(false);
            builder.setAutoReconnect(true);
            builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
            builder.setActivity(Activity.playing(tlumaczenia.get("status.starting")));
            builder.addEventListeners(eventWaiter, new ExceptionListener());
            builder.setBulkDeleteSplittingEnabled(false);
            builder.setCallbackPool(Executors.newFixedThreadPool(4));
            builder.enableCache(CacheFlag.EMOTE, CacheFlag.ACTIVITY);
            MessageAction.setDefaultMentions(EnumSet.of(Message.MentionType.EMOTE, Message.MentionType.CHANNEL));
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

        databaseManager = new DatabaseManager();
        databaseManager.load();

        try {
            Thread.sleep(8000);
        } catch (InterruptedException ignored) {}

        RedisManager     redisManager        = new RedisManager(shard.get().getSelfUser().getIdLong());

        CaseDao          caseDao             = new CaseDao(databaseManager);
        UserDao          userDao             = new UserDao(databaseManager);
        NieobecnosciDao  nieobecnosciDao     = new NieobecnosciDao(databaseManager);
        RemindDao        remindDao           = new RemindDao(databaseManager);
        GiveawayDao      giveawayDao         = new GiveawayDao(databaseManager);
        StatsDao         statsDao            = new StatsDao(databaseManager);
        VoiceStateDao    voiceStateDao       = new VoiceStateDao(databaseManager);
        MultiDao         multiDao            = new MultiDao(databaseManager);

        ArrayList<Object> listeners = new ArrayList<>();
        CommandExecute commandExecute = new CommandExecute(commandManager, tlumaczenia, argumentManager, userDao);
        listeners.add(commandExecute);
        listeners.forEach(api::addEventListener);

        this.modulManager = new ModulManager();
        ModLog modLog = new ModLog(api, caseDao);
        NieobecnosciManager nieobecnosciManager = new NieobecnosciManager(api, nieobecnosciDao);
        api.addEventListener(modLog);

        this.musicModule = new MusicModule(commandManager, api, eventWaiter, voiceStateDao);
        this.statsModule = new StatsModule(commandManager, api, eventWaiter, statsDao, musicModule, nieobecnosciDao);

        APIModule apiModule = new APIModule(api, caseDao, redisManager, nieobecnosciDao, statsDao);

        modulManager.getModules().add(new LogsModule(api, statsModule));
        modulManager.getModules().add(new ChatModule(api, karyJSON, caseDao, modLog, statsModule));
//        modulManager.getModules().add(new StatusModule(api));
        modulManager.getModules().add(new NieobecnosciModule(api, nieobecnosciDao, nieobecnosciManager));
        modulManager.getModules().add(new LiczydloModule(api));
        modulManager.getModules().add(new CommandsModule(commandManager, tlumaczenia, api, eventWaiter, karyJSON, caseDao, modulManager, commandExecute, userDao, modLog, nieobecnosciDao, remindDao, giveawayDao, statsModule, musicModule, multiDao));
        modulManager.getModules().add(musicModule);
        modulManager.getModules().add(statsModule);
        modulManager.getModules().add(apiModule);
        modulManager.getModules().add(new WeryfikacjaModule(apiModule));

        for (Modul modul : modulManager.getModules()) {
            try {
                int commands = commandManager.getCommands().size();
                Log.debug(tlumaczenia.get("module.loading", modul.getName()));
                boolean bol = false;
                try {
                    bol = modul.startUp();
                    modul.setStart(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                commands = commandManager.getCommands().size() - commands;
                if (!bol)
                    Log.error(tlumaczenia.get("module.loading.fail"));
                else
                    Log.debug(tlumaczenia.get("module.loading.success", modul.getName(), commands));

                moduls.put(modul.getName(), modul);
            } catch (Exception ignored) {
                Log.error(tlumaczenia.get("module.loading.fail"));
            }
        }


        api.setStatus(OnlineStatus.ONLINE);
        api.setActivity(Activity.playing(tlumaczenia.get("status.hi", WERSJA)));
        Log.info("Zalogowano jako %s", shard.get().getSelfUser());

        if (api.getGuildById(Ustawienia.instance.bot.guildId) == null) {
            api.shutdown();
            Log.newError("Nie ma bota na serwerze docelowym");
            System.exit(1);
        }
    }

    public void shutdownThread() {
        this.shutdownThread = new Thread(() -> {
            Log.info("Zamykam...");
            RebootCommand.reboot = true;
            api.setStatus(OnlineStatus.DO_NOT_DISTURB);
            api.setActivity(Activity.playing("Wyłącznie bota w toku..."));

            musicModule.load();
            modulManager.disableAll();
            statsModule.getStatsCache().databaseSave();
        });
        shutdownThread.setName("P2WB0T ShutDown");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }


}
