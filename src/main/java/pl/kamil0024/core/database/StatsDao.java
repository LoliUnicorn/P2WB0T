package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.StatsConfig;

import java.util.List;

public class StatsDao implements Dao<StatsConfig> {

    private final PgMapper<StatsConfig> mapper;

    public StatsDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(StatsConfig.class);
    }

    @Override
    public StatsConfig get(String id) {
        return mapper.load(id).orElseGet(() -> new StatsConfig(id));
    }

    @Override
    public void save(StatsConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<StatsConfig> getAll() {
        return mapper.loadAll();
    }
}
