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

package pl.kamil0024.core.util.kary;

public enum KaryEnum {
    KICK, BAN, MUTE, TEMPBAN, TEMPMUTE, UNMUTE, UNBAN;

    public static String getName(KaryEnum kara) {
        if (kara == KICK) return "Kick";
        if (kara == BAN) return "Ban";
        if (kara == MUTE) return "Mute";
        if (kara == TEMPBAN) return "Tempban";
        if (kara == TEMPMUTE) return "Tempmute";
        if (kara == UNBAN) return "Unban";
        if (kara == UNMUTE) return "Unmute";
        return String.valueOf(kara);
    }

    public static KaryEnum getKara(String kara) {
        kara = kara.toLowerCase();
        if (kara.equals("kick")) return KICK;
        if (kara.equals("ban")) return BAN;
        if (kara.equals("mute")) return MUTE;
        if (kara.equals("tempban")) return TEMPBAN;
        if (kara.equals("tempmute")) return TEMPMUTE;
        throw new UnsupportedOperationException("Typ kary " + kara + " nie istnieje!");
    }

}
