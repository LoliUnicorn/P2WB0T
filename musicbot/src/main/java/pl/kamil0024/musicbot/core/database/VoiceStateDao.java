package pl.kamil0024.musicbot.core.database;

import gg.amy.pgorm.PgMapper;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.musicbot.core.database.config.Dao;
import pl.kamil0024.musicbot.core.database.config.VoiceStateConfig;

import java.util.List;

public class VoiceStateDao implements Dao<VoiceStateConfig> {

    private final PgMapper<VoiceStateConfig> mapper;

    public VoiceStateDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(VoiceStateConfig.class);
    }

    @Override
    @Nullable
    public VoiceStateConfig get(String id) {
        return mapper.load(id).orElse(null);
    }

    @Override
    public void save(VoiceStateConfig toCos) {
        mapper.save(toCos);
    }

    public void delete() {
        mapper.delete(1);
    }

    @Override
    public List<VoiceStateConfig> getAll() {
        return null;
    }
}
