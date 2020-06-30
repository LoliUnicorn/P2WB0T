package pl.kamil0024.logs;

import net.dv8tion.jda.api.JDA;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.logs.logger.Logger;
import pl.kamil0024.logs.logger.MessageManager;

import javax.inject.Inject;

public class LogsModule implements Modul {
    
    @Inject JDA api;

    private boolean start = false;

    private MessageManager messageManager;
    private Logger logger;

    public LogsModule(JDA api) {
        this.api = api;
    }
    
    @Override
    public boolean startUp() {
        messageManager = new MessageManager();
        logger = new Logger(messageManager, api);
        api.addEventListener(messageManager, logger);
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
