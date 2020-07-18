package pl.kamil0024.logs;

import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.logs.logger.ClearCache;
import pl.kamil0024.logs.logger.Logger;
import pl.kamil0024.logs.logger.MessageManager;
import pl.kamil0024.stats.StatsModule;

import javax.inject.Inject;
import java.util.Timer;

public class LogsModule implements Modul {
    
    @Inject ShardManager api;

    private boolean start = false;

    private MessageManager messageManager;
    private Logger logger;
    private StatsModule statsModule;

    public LogsModule(ShardManager api, StatsModule statsModule) {
        this.api = api;
        this.statsModule = statsModule;
    }
    
    @Override
    public boolean startUp() {
        messageManager = new MessageManager();
        logger = new Logger(messageManager, api, statsModule);
        api.addEventListener(messageManager, logger);
        Timer t = new Timer();
        t.schedule(new ClearCache(messageManager), 10000);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(messageManager, logger);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public boolean isStart() {
        return this.start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

}
