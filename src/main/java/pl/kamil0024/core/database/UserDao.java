package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.UserConfig;

import java.util.List;

public class UserDao implements Dao<UserConfig> {

    private final PgMapper<UserConfig> mapper;

    public UserDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(UserConfig.class);
    }

    @Override
    public UserConfig get(String id) {
        return mapper.load(id).orElseGet(() -> new UserConfig(id));
    }

    @Override
    public void save(UserConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<UserConfig> getAll() {
        return mapper.loadAll();
    }

}
