package pl.kamil0024.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import pl.kamil0024.chat.ChatModule;
import pl.kamil0024.commands.CommandsModule;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.arguments.ArgumentManager;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.database.*;
import pl.kamil0024.core.database.config.UserConfig;
import pl.kamil0024.core.listener.ExceptionListener;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.Statyczne;
import pl.kamil0024.core.util.Tlumaczenia;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.liczydlo.LiczydloModule;
import pl.kamil0024.logs.LogsModule;
import pl.kamil0024.nieobecnosci.NieobecnosciModule;
import pl.kamil0024.status.StatusModule;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

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

    public B0T(String token) {
        moduls = new HashMap<>();
        tlumaczenia = new Tlumaczenia();
        argumentManager = new ArgumentManager();
        commandManager = new CommandManager();

        argumentManager.registerAll();

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

        JDA api = null;
        try {
            api = new JDABuilder(token).
                    setActivity(Activity.playing(tlumaczenia.get("status.starting")))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB).addEventListeners(eventWaiter, new ExceptionListener())
                    .build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            Log.error("Nie udalo sie zalogowac!");
            e.printStackTrace();
            System.exit(1);
        }

        databaseManager = new DatabaseManager();
        databaseManager.load();

        CaseDao caseDao = new CaseDao(databaseManager);
        UserDao userDao = new UserDao(databaseManager);
        NieobecnosciDao nieobecnosciDao = new NieobecnosciDao(databaseManager);
        RemindDao remindDao = new RemindDao(databaseManager);

        ArrayList<Object> listeners = new ArrayList<>();
        CommandExecute commandExecute = new CommandExecute(commandManager, tlumaczenia, argumentManager, userDao);
        listeners.add(commandExecute);
        listeners.forEach(api::addEventListener);

        ModulManager modulManager = new ModulManager();
        ModLog modLog = new ModLog(api, caseDao);
        api.addEventListener(modLog);

        modulManager.getModules().add(new LogsModule(api));
        modulManager.getModules().add(new ChatModule(api, karyJSON, caseDao, modLog));
//        modulManager.getModules().add(new StatusModule(api));
        modulManager.getModules().add(new NieobecnosciModule(api, nieobecnosciDao));
        modulManager.getModules().add(new LiczydloModule(api));
        modulManager.getModules().add(new CommandsModule(commandManager, tlumaczenia, api, eventWaiter, karyJSON, caseDao, modulManager, commandExecute, userDao, modLog, nieobecnosciDao, remindDao));

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

        api.getSelfUser().getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        api.getSelfUser().getJDA().getPresence().setActivity(Activity.playing(tlumaczenia.get("status.hi", WERSJA)));
        Log.info("Zalogowano jako %s", api.getSelfUser().getName());

        if (api.getGuildById(Ustawienia.instance.bot.guildId) == null) {
            api.shutdown();
            Log.newError("Nie ma bota na serwerze docelowym");
            System.exit(1);
        }

    }
}
