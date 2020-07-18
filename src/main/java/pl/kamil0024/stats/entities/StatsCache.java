package pl.kamil0024.stats.entities;

import lombok.Data;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
public class StatsCache {

    private HashMap<String, Statystyka> statystykaMap;

    private StatsDao statsDao;

    public StatsCache(StatsDao statsDao) {
        this.statystykaMap = new HashMap<>();
        this.statsDao = statsDao;

        ScheduledExecutorService executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::tak, 0, 5, TimeUnit.MINUTES);
    }

    public void save(String id, Statystyka stats) {
        stats.setDay(new BDate().getDateTime().getDayOfYear());
        getStatystykaMap().remove(id);
        getStatystykaMap().put(id, stats);
    }

    public void add(String id, Statystyka stats) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());

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
        Log.debug("Zapisuje statystyki adminów...");
        for (Map.Entry<String, Statystyka> entry : getStatystykaMap().entrySet()) {
            StatsConfig sc = statsDao.get(entry.getKey());
            Statystyka dzisiaj = StatsConfig.getStatsFromDay(sc.getStats(), entry.getValue().getDay());
            if (dzisiaj == null) {
                sc.getStats().add(entry.getValue());
            } else {
                sc.getStats().remove(dzisiaj);
                sc.getStats().add(entry.getValue());
            }
            statsDao.save(sc);
        }
    }

    public synchronized void addZmutowanych(String id, int liczba) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());
        statystyka.setZmutowanych(liczba + statystyka.getZmutowanych());
        save(id, statystyka);
    }

    public synchronized void addZbanowanych(String id, int liczba) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());
        statystyka.setZbanowanych(liczba + statystyka.getZbanowanych());
        save(id, statystyka);
    }

    public synchronized void addUsunietychWiadomosci(String id, int liczba) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());
        statystyka.setUsunietychWiadomosci(liczba + statystyka.getUsunietychWiadomosci());
        save(id, statystyka);
    }

    public synchronized void addNapisanychWiadomosci(String id, int liczba) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());
        statystyka.setNapisanychWiadomosci(liczba + statystyka.getNapisanychWiadomosci());
        save(id, statystyka);
    }

    public synchronized void addWyrzuconych(String id, int liczba) {
        Statystyka statystyka = getStatystykaMap().getOrDefault(id, new Statystyka());
        statystyka.setWyrzuconych(liczba + statystyka.getWyrzuconych());
        save(id, statystyka);
    }

}
