package pl.kamil0024.musicbot.music;

import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.musicbot.core.module.Modul;
import pl.kamil0024.musicbot.music.managers.MusicManager;

public class MusicModule implements Modul {

    private ShardManager api;
    private boolean start = false;
    private MusicManager musicManager;

    public MusicModule(ShardManager api, MusicManager musicManager) {
        this.api = api;
        this.musicManager = musicManager;
    }

    @Override
    public boolean startUp() {
        return true;
    }

    @Override
    public boolean shutDown() {
        return true;
    }

    @Override
    public String getName() {
        return "music";
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
