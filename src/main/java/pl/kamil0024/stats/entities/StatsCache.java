package pl.kamil0024.stats.entities;

import lombok.Data;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;

import java.util.HashMap;
import java.util.Map;

@Data
public class StatsCache {

    private HashMap<String, Statystyka> statystykaMap;

    private StatsDao statsDao;

    public StatsCache(StatsDao statsDao) {
        this.statystykaMap = new HashMap<>();
        this.statsDao = statsDao;
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

        save(id, stats);
    }

    public synchronized void databaseSave() {
        for (Map.Entry<String, Statystyka> entry : getStatystykaMap().entrySet()) {
            StatsConfig sc = statsDao.get(entry.getKey());

            Statystyka dzisiaj = StatsConfig.getStatsFromDay(sc.getStats(), new BDate().getTimestamp());
            if (dzisiaj == null) {
                Log.debug("statsCache 1");
                sc.getStats().add(entry.getValue());
            } else if (dzisiaj.getDay() == new BDate().getDateTime().getDayOfYear()) {
                Log.debug("statsCache 2");
                sc.getStats().remove(dzisiaj);
                dzisiaj.setNapisanychWiadomosci(entry.getValue().getNapisanychWiadomosci() + dzisiaj.getNapisanychWiadomosci());
                dzisiaj.setUsunietychWiadomosci(entry.getValue().getUsunietychWiadomosci() + dzisiaj.getUsunietychWiadomosci());
                dzisiaj.setZbanowanych(entry.getValue().getZbanowanych() + dzisiaj.getZbanowanych());
                dzisiaj.setZmutowanych(entry.getValue().getZmutowanych() + dzisiaj.getZmutowanych());
                sc.getStats().add(dzisiaj);
            }
            statsDao.save(sc);
        }
    }

    public void addZmutowanych(String id, int liczba) {
        Statystyka statystyka = new Statystyka();
        statystyka.setZmutowanych(liczba);
        add(id, statystyka);
    }

    public void addZbanowanych(String id, int liczba) {
        Statystyka statystyka = new Statystyka();
        statystyka.setZmutowanych(liczba);
        add(id, statystyka);
    }

    public void addUsunietychWiadomosci(String id, int liczba) {
        Statystyka statystyka = new Statystyka();
        statystyka.setUsunietychWiadomosci(liczba);
        add(id, statystyka);
    }

    public void addNapisanychWiadomosci(String id, int liczba) {
        Statystyka statystyka = new Statystyka();
        statystyka.setNapisanychWiadomosci(liczba);
        add(id, statystyka);
    }

}
