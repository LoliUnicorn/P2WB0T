package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.UserConfig;

import java.util.ArrayList;
import java.util.List;

public class CaseDao implements Dao<CaseConfig> {

    private final PgMapper<CaseConfig> mapper;

    public CaseDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(CaseConfig.class);
    }

    @Override
    public CaseConfig get(String id) {
        return mapper.load(id).orElseGet(() -> new CaseConfig(id));
    }

    public CaseConfig get(int id) {
        return mapper.load(String.valueOf(id)).orElseGet(() -> new CaseConfig(String.valueOf(id)));
    }

    @Override
    public synchronized void save(CaseConfig kara) {
        mapper.save(kara);
    }

    @Override
    public List<CaseConfig> getAll() {
        return mapper.loadAll();
    }

    public List<CaseConfig> getAktywe(String id) {
        return mapper.getAktywne(id);
    }

    public List<CaseConfig> getNickAktywne(String nick) {
        if (nick.equals("-")) return new ArrayList<>();
        return mapper.getMcAktywne(nick);
    }

    public List<CaseConfig> getAllAktywne() {
        return mapper.getAllAktywne();
    }

    public void delete(int id) {
        mapper.delete(id);
    }

    public List<CaseConfig> getAllPunAktywne(String id) {
        return mapper.getAllPunAktywne(id);
    }

    public List<CaseConfig> getAll(String userId) {
        return mapper.getAll(userId);
    }

}
