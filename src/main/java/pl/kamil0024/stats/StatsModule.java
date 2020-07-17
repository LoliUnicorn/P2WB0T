package pl.kamil0024.stats;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.stats.commands.StatsCommand;
import pl.kamil0024.stats.entities.StatsCache;

import java.util.ArrayList;

public class StatsModule implements Modul {

    private ArrayList<Command> cmd;

    @Inject CommandManager commandManager;
    @Inject ShardManager api;
    @Inject EventWaiter eventWaiter;
    @Inject StatsDao statsDao;

    private boolean start = false;

    @Getter public StatsCache statsCache;

    public StatsModule(CommandManager commandManager, ShardManager api, EventWaiter eventWaiter, StatsDao statsDao) {
        this.commandManager = commandManager;
        this.api = api;
        this.eventWaiter = eventWaiter;
        this.statsDao = statsDao;
        this.statsCache = new StatsCache(statsDao);

        for (StatsConfig statsConfig : statsDao.getAll()) {
            statsCache.save(statsConfig.getId(), StatsConfig.getStatsFromDay(statsConfig.getStats(), new BDate().getTimestamp()));
        }

        new Thread(() -> {
            test();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) { }
            test();
        }).start();

    }

    private void test() {
        Log.debug(" = Symulacja = ");
        getStatsCache().addNapisanychWiadomosci("343467373417857025", 5);
//        getStatsCache().addZmutowanych("343467373417857025", 15);
//        getStatsCache().addZbanowanych("343467373417857025", 10);

        getStatsCache().databaseSave();
        Log.debug(" = Symulacja = ");

    }

    @Override
    public boolean startUp() {
        cmd = new ArrayList<>();

        cmd.add(new StatsCommand(statsDao));

        cmd.forEach(commandManager::registerCommand);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        commandManager.unregisterCommands(cmd);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

}
