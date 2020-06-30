package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.RemindConfig;

import java.util.List;

public class RemindDao implements Dao<RemindConfig> {

    private final PgMapper<RemindConfig> mapper;

    public RemindDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(RemindConfig.class);
    }


    @Override
    public RemindConfig get(String id) {
        return mapper.load(id).orElseGet(() -> new RemindConfig(id));
    }

    @Override
    public void save(RemindConfig toCos) {
        mapper.save(toCos);
    }

    @Override

    public List<RemindConfig> getAll() {
        return mapper.loadAll();
    }

    public void remove(RemindConfig toCos) {
        mapper.delete(Integer.parseInt(toCos.getId()));
    }

    public String getNextId() {
        return String.valueOf(getAll().size() + 1);
    }

}
