package pl.kamil0024.status;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.status.listeners.ChangeNickname;
import pl.kamil0024.status.statusy.CheckMute;
import pl.kamil0024.status.statusy.CheckUnmute;

import java.util.Timer;

public class StatusModule implements Modul {

    private boolean start = false;
    private ChangeNickname changeNickname;

    private final ShardManager api;


    public StatusModule(ShardManager api) {
        this.api = api;
    }

    @Override
    public boolean startUp() {
        this.changeNickname = new ChangeNickname();
        Timer tim = new Timer();
        tim.schedule(new CheckMute(api), 0, 120000); // 300000
        tim.schedule(new CheckUnmute(api), 0, 120000); // 180000
        api.addEventListener(changeNickname);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(changeNickname);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        start = bol;
    }
}
