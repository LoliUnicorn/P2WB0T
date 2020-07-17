package pl.kamil0024.stats.entities;

import lombok.Data;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;

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
        Statystyka stat = stats;
        stat.setDay(new BDate().getDateTime().getDayOfYear());
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

            Statystyka dzisiaj = StatsConfig.getStatsFromDay(sc.getStats(), new BDate().getDateTime().getDayOfYear());
            if (dzisiaj == null) {
                sc.getStats().add(entry.getValue());
            } else {
                dzisiaj.setNapisanychWiadomosci(entry.getValue().getNapisanychWiadomosci() + dzisiaj.getNapisanychWiadomosci());
                dzisiaj.setUsunietychWiadomosci(entry.getValue().getUsunietychWiadomosci() + dzisiaj.getUsunietychWiadomosci());
                dzisiaj.setZbanowanych(entry.getValue().getZbanowanych() + dzisiaj.getZbanowanych());
                dzisiaj.setZmutowanych(entry.getValue().getZmutowanych() + dzisiaj.getZmutowanych());
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
