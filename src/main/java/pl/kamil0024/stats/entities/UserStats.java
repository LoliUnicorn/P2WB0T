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

package pl.kamil0024.stats.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.kamil0024.bdate.BDate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@Getter
@Setter
public class UserStats {

    public UserStats(String id) {
        this.id = id;
    }

    private String id;

    //   dzien     statystyka
    Map<Integer, Statystyka> statsMap = new HashMap<>();

    public void add(StatsType statsType, int count) {
        int day = getDay();
        Statystyka stats = Optional.of(getStatsMap().get(day)).orElse(newStats());
        switch (statsType) {
            case MUTE:
                stats.setZmutowanych(stats.getZmutowanych() + count);
                break;
            case BAN:
                stats.setZbanowanych(stats.getZbanowanych() + count);
                break;
            case KICK:
                stats.setWyrzuconych(stats.getWyrzuconych() + count);
                break;
            case DELETEDMESSAGE:
                stats.setUsunietychWiadomosci(stats.getUsunietychWiadomosci() + count);
                break;
            case SENDMESSAGE:
                stats.setNapisanychWiadomosci(stats.getNapisanychWiadomosci() + count);
                break;
        }
        getStatsMap().remove(day);
        getStatsMap().put(day, stats);
    }

    private int getDay() {
        return new BDate().getDateTime().getDayOfYear();
    }

    private Statystyka newStats() {
        Statystyka stat = new Statystyka();
        stat.setDay(getDay());
        return stat;
    }

    public Statystyka getFromNow() {
        Statystyka xd = getStatsMap().get(getDay());
        return xd == null ? newStats() : xd;
    }

    public enum StatsType {
        MUTE, BAN, KICK, DELETEDMESSAGE, SENDMESSAGE
    }


}
