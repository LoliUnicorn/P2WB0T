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

package pl.kamil0024.commands;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.commands.kolkoikrzyzyk.KolkoIKrzyzykManager;
import pl.kamil0024.commands.listener.GiveawayListener;
import pl.kamil0024.commands.zabawa.KolkoIKrzyzykCommand;
import pl.kamil0024.commands.zabawa.PogodaCommand;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.Tlumaczenia;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.stats.StatsModule;
import pl.kamil0024.commands.dews.*;
import pl.kamil0024.commands.moderation.*;
import pl.kamil0024.commands.system.*;
import pl.kamil0024.core.database.*;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandsModule implements Modul {

    private ArrayList<Command> cmd;

    @Inject CommandManager commandManager;
    @Inject Tlumaczenia tlumaczenia;
    @Inject ShardManager api;
    @Inject EventWaiter eventWaiter;
    @Inject KaryJSON karyJSON;
    @Inject CaseDao caseDao;
    @Inject ModulManager modulManager;
    @Inject CommandExecute commandExecute;
    @Inject UserDao userDao;
    @Inject NieobecnosciDao nieobecnosciDao;
    @Inject RemindDao remindDao;
    @Inject GiveawayDao giveawayDao;
    @Inject StatsModule statsModule;
    @Inject MusicModule musicModule;
    @Inject MultiDao multiDao;
    @Inject MusicAPI musicAPI;

    private boolean start = false;
    private ModLog modLog;

    // Listeners

    KolkoIKrzyzykManager kolkoIKrzyzykManager;

    public CommandsModule(CommandManager commandManager, Tlumaczenia tlumaczenia, ShardManager api, EventWaiter eventWaiter, KaryJSON karyJSON, CaseDao caseDao, ModulManager modulManager, CommandExecute commandExecute, UserDao userDao, ModLog modLog, NieobecnosciDao nieobecnosciDao, RemindDao remindDao, GiveawayDao giveawayDao, StatsModule statsModule, MusicModule musicModule, MultiDao multiDao, MusicAPI musicAPI) {
        this.commandManager = commandManager;
        this.tlumaczenia = tlumaczenia;
        this.api = api;
        this.eventWaiter = eventWaiter;
        this.karyJSON = karyJSON;
        this.caseDao = caseDao;
        this.modulManager = modulManager;
        this.commandExecute = commandExecute;
        this.userDao = userDao;
        this.modLog = modLog;
        this.nieobecnosciDao = nieobecnosciDao;
        this.remindDao = remindDao;
        this.giveawayDao = giveawayDao;
        this.statsModule = statsModule;
        this.musicModule = musicModule;
        this.multiDao = multiDao;
        this.musicAPI = musicAPI;

        ScheduledExecutorService executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::tak, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean startUp() {
        GiveawayListener giveawayListener = new GiveawayListener(giveawayDao, api);
        kolkoIKrzyzykManager = new KolkoIKrzyzykManager(api, eventWaiter);

        cmd = new ArrayList<>();

        cmd.add(new PingCommand());
        cmd.add(new BotinfoCommand(commandManager, modulManager, musicAPI));
        cmd.add(new HelpCommand(commandManager));
        cmd.add(new PoziomCommand());
        cmd.add(new EvalCommand(eventWaiter, commandManager, caseDao, modLog, karyJSON, tlumaczenia, commandExecute, userDao, nieobecnosciDao, remindDao, modulManager, giveawayListener, giveawayDao, statsModule, multiDao, musicModule, musicAPI));
        cmd.add(new ForumCommand());
        cmd.add(new UserinfoCommand());
        cmd.add(new McpremiumCommand());
        cmd.add(new RemindmeCommand(remindDao, eventWaiter));
        cmd.add(new ModulesCommand(modulManager));
        cmd.add(new ClearCommand(statsModule));
        cmd.add(new CytujCommand());
        cmd.add(new CheckCommand(caseDao));
        cmd.add(new GiveawayCommand(giveawayDao, eventWaiter, giveawayListener));
        cmd.add(new RebootCommand());
        cmd.add(new ShellCommand());
        cmd.add(new ArchiwizujCommand());
        cmd.add(new MultiCommand(multiDao, eventWaiter));
        cmd.add(new PogodaCommand());
        cmd.add(new KolkoIKrzyzykCommand(kolkoIKrzyzykManager));

        // Moderacyjne:
        cmd.add(new StatusCommand(eventWaiter));
        cmd.add(new KarainfoCommand(caseDao));
        cmd.add(new UnmuteCommand(caseDao, modLog));
        cmd.add(new TempmuteCommand(caseDao, modLog, statsModule));
        cmd.add(new PunishCommand(karyJSON, eventWaiter, caseDao, modLog, statsModule));
        cmd.add(new KickCommand(caseDao, modLog, statsModule));
        cmd.add(new BanCommand(caseDao, modLog, statsModule));
        cmd.add(new TempbanCommand(caseDao, modLog, statsModule));
        cmd.add(new UnbanCommand(caseDao, modLog));
        cmd.add(new MuteCommand(caseDao, modLog, statsModule));
        cmd.add(new HistoryCommand(caseDao, eventWaiter));

        cmd.forEach(commandManager::registerCommand);
        setStart(true);
        return true;
    }

    private void tak() {
            RemindmeCommand.check(remindDao, api);
        }

    @Override
    public boolean shutDown() {
        kolkoIKrzyzykManager.stop();
        commandManager.unregisterCommands(cmd);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
            return "commands";
        }

    @Override
    public boolean isStart() {
            return start;
        }

    @Override
    public void setStart(boolean bol) {
            this.start = bol;
        }

    @Data
    @AllArgsConstructor
    public class KaraJSON {
        private final int id;
        private final String powod;
    }

    @Data
    @AllArgsConstructor
    public class WarnJSON {
        private final int warns;
        private final String kara;
        private final String czas;
    }

}
