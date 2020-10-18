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

import java.util.*;

public class CaseRedisManager {

    private final CaseDao caseDao;
    private final Cache<Integer> karyWRoku;

    public CaseRedisManager(RedisManager redisManager, CaseDao caseDao) {
        this.caseDao = caseDao;

        this.karyWRoku = redisManager.new CacheRetriever<Integer>(){}.getCache(-1);
    }

    public void load() {
        Map<Integer, Integer> karyWMiesiacu = new HashMap<>();
        Map<Long, Integer> karyWTygodniu = new HashMap<>();

        Map<Integer, Integer> ostatnieKary24h = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        DateTime now = new DateTime();
        for (CaseConfig caseConfig : caseDao.getAll()) {
            Kara kara = caseConfig.getKara();
            DateTime dt = new DateTime(kara.getTimestamp());
            
            int h = dt.getMonthOfYear();
            int kary = (karyWMiesiacu.getOrDefault(h, 0)) + 1;
            karyWMiesiacu.put(h, kary);

            if (dt.isAfter(now.minusDays(7))) {
                cal.setTime(new Date(dt.getMillis()));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long ms = cal.toInstant().toEpochMilli();
                karyWTygodniu.put(ms, (karyWTygodniu.getOrDefault(ms, 0)) + 1);
            }

            if (dt.isAfter(now.minusDays(1)) && dt.getDayOfYear() == now.getDayOfYear()) {
                int hofday = dt.getHourOfDay();
                ostatnieKary24h.put(hofday, (ostatnieKary24h.getOrDefault(dt.getHourOfDay(), 0)) + 1);
            }

        }

        for (Map.Entry<Integer, Integer> entry : karyWMiesiacu.entrySet()) {
            save(entry.getKey(), entry.getValue());
        }

        Log.debug("=-=-= Kary W Tygodniu =-=-=-");
        for (Map.Entry<Long, Integer> entry : karyWTygodniu.entrySet()) {
            Log.debug("Dzień: " + new Date(entry.getKey()));
            Log.debug("Kary: " + entry.getValue());
        }
        Log.debug("=-=-= Kary W Tygodniu =-=-=-");

        Log.debug("=-=-= Kary Dzisiaj =-=-=-");
        for (Map.Entry<Integer, Integer> entry : ostatnieKary24h.entrySet()) {
            Log.debug("Godzina: " + entry.getKey() + ":00");
            Log.debug("Kary: " + entry.getValue());
        }
        Log.debug("=-=-= Kary Dzisiaj =-=-=-");

    }

    public Integer get(String key) {
        return karyWRoku.getIfPresent(key);
    }

    public void save(int key, Integer conf) {
        karyWRoku.put(String.valueOf(key), conf);
    }

}
