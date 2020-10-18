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

package pl.kamil0024.api.redisstats.modules;

import org.joda.time.DateTime;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.redis.Cache;
import pl.kamil0024.core.redis.RedisManager;
import pl.kamil0024.core.util.kary.Kara;

import java.util.HashMap;
import java.util.Map;

public class CaseRedisManager {

    private final CaseDao caseDao;
    private final Cache<Integer> karyWRoku;

    public CaseRedisManager(RedisManager redisManager, CaseDao caseDao) {
        this.caseDao = caseDao;

        this.karyWRoku = redisManager.new CacheRetriever<Integer>(){}.getCache(-1);
    }

    public void load() {
        Map<Integer, Integer> karyWMiesiacu = new HashMap<>();
        for (CaseConfig caseConfig : caseDao.getAll()) {
            Kara kara = caseConfig.getKara();
            DateTime dt = new DateTime(kara.getTimestamp());
            int h = dt.getMonthOfYear();
            int kary = karyWMiesiacu.getOrDefault(h, 0);
            kary++;
            karyWMiesiacu.put(h, kary);
        }
        for (Map.Entry<Integer, Integer> entry : karyWMiesiacu.entrySet()) {
            Log.debug("Miesiac: " + entry.getKey());
            Log.debug("Kary: " + entry.getValue());
            Log.debug("--------------------");
            save(entry.getKey(), entry.getValue());
        }
    }

    public Integer get(String key) {
        return karyWRoku.getIfPresent(key);
    }

    public void save(int key, Integer conf) {
        karyWRoku.put(String.valueOf(key), conf);
    }

}
