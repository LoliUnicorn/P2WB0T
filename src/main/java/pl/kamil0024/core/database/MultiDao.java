package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.MultiConfig;

import java.util.List;

public class MultiDao implements Dao<MultiConfig> {

    private final PgMapper<MultiConfig> mapper;

    public MultiDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(MultiConfig.class);
    }

    @Override
    public MultiConfig get(String id) {
        return mapper.load(id).orElseGet(() -> new MultiConfig(id));
    }

    @Override
    public void save(MultiConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<MultiConfig> getAll() {
        return mapper.loadAll();
    }

}
