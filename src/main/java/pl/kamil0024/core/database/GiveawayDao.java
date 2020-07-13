package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.GiveawayConfig;

import java.util.List;

public class GiveawayDao implements Dao<GiveawayConfig> {

    private final PgMapper<GiveawayConfig> mapper;

    public GiveawayDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(GiveawayConfig.class);
    }


    @Override
    @Nullable
    public GiveawayConfig get(String id) {
        return mapper.load(id).orElse(null);
    }

    public GiveawayConfig get() {
        return new GiveawayConfig(String.valueOf(mapper.loadAll().size() + 1));
    }

    @Override
    public void save(GiveawayConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<GiveawayConfig> getAll() {
        return mapper.loadAll();
    }
}
