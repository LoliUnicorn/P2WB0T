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

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.kary.Kara;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class CaseRedisManager {

    @Setter private long lastUpdate = 0;

    private final CaseDao caseDao;
    private final Map<Long, Integer> mapKaryWRoku;
    private final Map<Long, Integer> mapWTygodniu;
    private final Map<Long, Integer> mapOstatnieKary24h;
    private final Map<Long, Integer> mapKaryWMiesiacu;

    private ScheduledExecutorService executorSche;

    public CaseRedisManager(CaseDao caseDao) {
        this.caseDao = caseDao;

        this.mapKaryWRoku = new HashMap<>();
        this.mapWTygodniu = new HashMap<>();
        this.mapOstatnieKary24h = new HashMap<>();
        this.mapKaryWMiesiacu = new HashMap<Long, Integer>();

        executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::load, 0, 1, TimeUnit.HOURS);
    }

    public void load() {
        getMapKaryWRoku().clear();
        getMapOstatnieKary24h().clear();
        getMapWTygodniu().clear();

        setLastUpdate(new Date().getTime());

        Map<Long, Integer> kryWRoku = new HashMap<>();
        Map<Long, Integer> karyWTygodniu = new HashMap<>();
        Map<Long, Integer> karyWMiesiacu = new HashMap<>();

        Map<Long, Integer> ostatnieKary24h = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        DateTime now = new DateTime();
        for (CaseConfig caseConfig : caseDao.getAll()) {
            Kara kara = caseConfig.getKara();
            DateTime dt = new DateTime(kara.getTimestamp());
            
            long h = dt.getMonthOfYear();
            int kary = (kryWRoku.getOrDefault(h, 0)) + 1;
            kryWRoku.put(h, kary);

            cal.setTime(new Date(dt.getMillis()));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            if (dt.isAfter(now.minusDays(8))) {
                long ms = cal.toInstant().toEpochMilli();
                karyWTygodniu.put(ms, (karyWTygodniu.getOrDefault(ms, 0)) + 1);
            }

            if (dt.isAfter(now.minusDays(1)) && dt.getDayOfYear() == now.getDayOfYear()) {
                long hofday = dt.getHourOfDay();
                ostatnieKary24h.put(hofday, (ostatnieKary24h.getOrDefault(hofday, 0)) + 1);
            }

            if (dt.getMonthOfYear() == now.getMonthOfYear() && dt.getYear() == now.getYear()) {
                long ms = cal.toInstant().toEpochMilli();
                karyWMiesiacu.put(ms, (karyWMiesiacu.getOrDefault(ms, 0)) + 1);
            }

        }

        for (Map.Entry<Long, Integer> entry : kryWRoku.entrySet()) {
            this.mapKaryWRoku.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Integer> entry : karyWTygodniu.entrySet()) {
            this.mapWTygodniu.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Integer> entry : ostatnieKary24h.entrySet()) { ;
            mapOstatnieKary24h.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Integer> entry : karyWMiesiacu.entrySet()) {
            mapKaryWMiesiacu.put(entry.getKey(), entry.getValue());
        }

    }
}
