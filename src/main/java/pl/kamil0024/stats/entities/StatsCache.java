package pl.kamil0024.stats.entities;

import com.google.gson.Gson;
import lombok.Data;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.stats.commands.StatsCommand;

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
            Statystyka dzisiaj = StatsConfig.getStatsFromDay(sc.getStats(), entry.getValue().getDay());
            if (dzisiaj == null) {
                Log.debug("dzisiaj jest nullem");
                sc.getStats().add(entry.getValue());
            } else {
                Log.debug("chyj kurwa");
                Log.debug(new Gson().toJson(entry.getValue()));
                Log.debug(new Gson().toJson(dzisiaj));
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

}
