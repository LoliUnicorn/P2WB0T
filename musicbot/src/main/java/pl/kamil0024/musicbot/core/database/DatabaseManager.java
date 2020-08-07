package pl.kamil0024.musicbot.core.database;

import gg.amy.pgorm.PgStore;
import pl.kamil0024.musicbot.core.Ustawienia;

@SuppressWarnings("unused")
public class DatabaseManager {

    private PgStore pgStore;

    public DatabaseManager () {}

    public void shutdown() {
        if (pgStore != null) pgStore.disconnect();
    }

    public PgStore getPgStore() {
        if (pgStore == null) throw new IllegalStateException("pgStore == null");
        return pgStore;
    }

    public void load() {
        Ustawienia ustawienia = Ustawienia.instance;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            /* lul */
        }
        pgStore = new PgStore(ustawienia.postgres.jdbcUrl, ustawienia.postgres.user, ustawienia.postgres.password);
        pgStore.connect();
    }

}
