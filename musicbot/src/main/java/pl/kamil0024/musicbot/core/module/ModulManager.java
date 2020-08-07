package pl.kamil0024.musicbot.core.module;

import lombok.Getter;

import java.util.ArrayList;

public class ModulManager {

    @Getter private final ArrayList<Modul> modules;

    public ModulManager() {
        this.modules = new ArrayList<>();
    }

    public void reloadAll() {
        modules.forEach(this::reload);
    }

    public void disableAll() {
        modules.forEach(Modul::shutDown);
    }

    public void startAll() {
        modules.forEach(this::start);
    }

    public void start(Modul modul) {
        if (!modul.isStart()) {
            modul.startUp();
        }
    }

    public void reload(Modul modul) {
        try {
            modul.shutDown();
            modul.startUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
