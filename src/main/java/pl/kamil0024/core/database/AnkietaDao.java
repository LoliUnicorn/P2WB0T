/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import pl.kamil0024.core.database.config.AnkietaConfig;
import pl.kamil0024.core.database.config.Dao;

import java.util.List;

public class AnkietaDao implements Dao<AnkietaConfig> {

    private final PgMapper<AnkietaConfig> mapper;

    public AnkietaDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(AnkietaConfig.class);
        pl.kamil0024.core.database.config.AnkietaConfig a = new pl.kamil0024.core.database.config.AnkietaConfig();
        a.setAutorId("xd");
        a.setId("xd");
        a.setDescription("kekW");
        a.setCreatedAt(0);
        a.setSendAt(0);
        a.getOpcje().add(new pl.kamil0024.core.database.config.AnkietaConfig.Opcja(1, "wybór 1", "😂"));
        a.getOpcje().add(new pl.kamil0024.core.database.config.AnkietaConfig.Opcja(2, "wybór 2", "😢"));
        a.getGlosy().put(1, 0);
        a.getGlosy().put(2, 0);
        save(a);
    }

    @Override
    public AnkietaConfig get(String id) {
        return mapper.load(id).orElse(null);
    }

    @Override
    public void save(AnkietaConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<AnkietaConfig> getAll() {
        return mapper.loadAll();
    }

}
