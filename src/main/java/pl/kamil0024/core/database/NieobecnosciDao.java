package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.NieobecnosciConfig;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;

import java.util.ArrayList;
import java.util.List;

public class NieobecnosciDao implements Dao<NieobecnosciConfig> {

    private final PgMapper<NieobecnosciConfig> mapper;

    public NieobecnosciDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(NieobecnosciConfig.class);
    }


    @Override
    public NieobecnosciConfig get(String id) {
        return mapper.load(id).orElseGet(() -> newObject(id));
    }

    @Override
    public void save(NieobecnosciConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<NieobecnosciConfig> getAll() {
        return mapper.loadAll();
    }

    private NieobecnosciConfig newObject(String id) {
        return new NieobecnosciConfig(id);
    }

    public int getNextId(String id) {
        return get(id).getNieobecnosc().size() + 1;
    }


    public ArrayList<Nieobecnosc> getAllAktywne() {
        ArrayList<Nieobecnosc> xd = new ArrayList<>();
        for (NieobecnosciConfig conf : getAll()) {
            for (Nieobecnosc nieobecnosc : conf.getNieobecnosc()) {
                assert nieobecnosc.isAktywna();
                xd.add(nieobecnosc);
            }
        }
        return xd;
    }

}
