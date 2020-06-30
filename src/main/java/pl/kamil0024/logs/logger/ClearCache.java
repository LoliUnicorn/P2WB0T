package pl.kamil0024.logs.logger;

import java.util.TimerTask;

public class ClearCache extends TimerTask {

    private final MessageManager manager;

    public ClearCache(MessageManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.getMap().clear();
    }
}
