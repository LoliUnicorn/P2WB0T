package pl.kamil0024.stats.entities;

import lombok.Data;
import org.w3c.dom.stylesheets.LinkStyle;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
@Data
public class StatsCache {

    private HashMap<String, Map<Integer, Statystyka> > statystykaMap;

    private StatsDao statsDao;

    public StatsCache(StatsDao statsDao) {
        this.statystykaMap = new HashMap<>();
        this.statsDao = statsDao;

        ScheduledExecutorService executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::tak, 0, 60, TimeUnit.MINUTES);
    }

    public void save(String id, Statystyka stats) {
        int day = new BDate().getDateTime().getDayOfYear();
        stats.setDay(day);
        getStatystykaMap().remove(id);
        Map<Integer, Statystyka> stat = new HashMap<>();
        stat.put(day, stats);
        getStatystykaMap().put(id, stat);
    }

    public void add(String id, Statystyka stats) {
        int day = new BDate().getDateTime().getDayOfYear();

        Map<Integer, Statystyka> stat = getStatystykaMap().get(id);
        Statystyka statystyka;
        if (stat == null || stat.isEmpty()) {
            statystyka = new Statystyka();
        } else {
            statystyka = stat.get(day);
            if (statystyka == null) statystyka = new Statystyka();
        }

        statystyka.setNapisanychWiadomosci(stats.getNapisanychWiadomosci() + statystyka.getNapisanychWiadomosci());
        statystyka.setUsunietychWiadomosci(stats.getUsunietychWiadomosci() + statystyka.getUsunietychWiadomosci());
        statystyka.setZbanowanych(stats.getZbanowanych() + statystyka.getZbanowanych());
        statystyka.setZmutowanych(stats.getZmutowanych() + statystyka.getZmutowanych());
        statystyka.setWyrzuconych(stats.getWyrzuconych() + statystyka.getWyrzuconych());

        save(id, stats);
    }

    public void tak() {
        databaseSave();
    }

    public synchronized void databaseSave() {
        int day = new BDate().getDateTime().getDayOfYear();
        for (Map.Entry<String, Map<Integer, Statystyka>> entry : getStatystykaMap().entrySet()) {
            StatsConfig sc = statsDao.get(entry.getKey());
            Statystyka dzisiaj = StatsConfig.getStatsFromDay(sc.getStats(), day);
            if (dzisiaj == null) {
                sc.getStats().add(entry.getValue().get(day));
            } else {
                sc.getStats().remove(dzisiaj);
                sc.getStats().add(entry.getValue().get(day));
            }
            statsDao.save(sc);
        }
    }

    public synchronized void addZmutowanych(String id, int liczba) {
        int day = new BDate().getDateTime().getDayOfYear();
        Statystyka stat;
        Map<Integer, Statystyka> statystyka = getStatystykaMap().get(id);
        if (statystyka == null) {
            stat = new Statystyka();
        } else if (statystyka.get(day) == null) {
            stat = new Statystyka();
        } else {
            stat = statystyka.get(day);
        }
        stat.setZmutowanych(liczba + stat.getZmutowanych());
        save(id, stat);
    }

    public synchronized void addZbanowanych(String id, int liczba) {
        int day = new BDate().getDateTime().getDayOfYear();
        Statystyka stat;
        Map<Integer, Statystyka> statystyka = getStatystykaMap().get(id);
        if (statystyka == null) {
            stat = new Statystyka();
        } else if (statystyka.get(day) == null) {
            stat = new Statystyka();
        } else {
            stat = statystyka.get(day);
        }
        stat.setZbanowanych(liczba + stat.getZbanowanych());
        save(id, stat);
    }

    public synchronized void addUsunietychWiadomosci(String id, int liczba) {
        int day = new BDate().getDateTime().getDayOfYear();
        Statystyka stat;
        Map<Integer, Statystyka> statystyka = getStatystykaMap().get(id);
        if (statystyka == null) {
            stat = new Statystyka();
        } else if (statystyka.get(day) == null) {
            stat = new Statystyka();
        } else {
            stat = statystyka.get(day);
        }
        stat.setUsunietychWiadomosci(liczba + stat.getUsunietychWiadomosci());
        save(id, stat);
    }

    public synchronized void addNapisanychWiadomosci(String id, int liczba) {
        int day = new BDate().getDateTime().getDayOfYear();
        Statystyka stat;
        Map<Integer, Statystyka> statystyka = getStatystykaMap().get(id);
        if (statystyka == null) {
            stat = new Statystyka();
        } else if (statystyka.get(day) == null) {
            stat = new Statystyka();
        } else {
            stat = statystyka.get(day);
        }
        stat.setNapisanychWiadomosci(liczba + stat.getNapisanychWiadomosci());
        save(id, stat);
    }

    public synchronized void addWyrzuconych(String id, int liczba) {
        int day = new BDate().getDateTime().getDayOfYear();
        Statystyka stat;
        Map<Integer, Statystyka> statystyka = getStatystykaMap().get(id);
        if (statystyka == null) {
            stat = new Statystyka();
        } else if (statystyka.get(day) == null) {
            stat = new Statystyka();
        } else {
            stat = statystyka.get(day);
        }
        stat.setWyrzuconych(liczba + stat.getWyrzuconych());
        save(id, stat);
    }

}
