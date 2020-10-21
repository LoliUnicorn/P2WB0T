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
import org.joda.time.DateTime;
import pl.kamil0024.core.database.config.ApelacjeConfig;
import pl.kamil0024.core.database.config.Dao;

import java.util.List;
import java.util.stream.Collectors;

public class ApelacjeDao implements Dao<ApelacjeConfig> {

    private final PgMapper<ApelacjeConfig> mapper;

    public ApelacjeDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(ApelacjeConfig.class);
    }

    @Override
    public ApelacjeConfig get(String id) {
        return mapper.load(id).orElse(null);
    }

    @Override
    public void save(ApelacjeConfig toCos) {
        mapper.save(toCos);
    }

    @Override
    public List<ApelacjeConfig> getAll() {
        return mapper.loadAll();
    }

    public static List<ApelacjeConfig> getFromMonth(List<ApelacjeConfig> apelacje, int month, int year) {
        return apelacje.stream().filter(a -> {
            DateTime dt = new DateTime(a.getCreatedTime());
            return dt.getMonthOfYear() == month && dt.getYear() == year;
        }).collect(Collectors.toList());
    }



}
