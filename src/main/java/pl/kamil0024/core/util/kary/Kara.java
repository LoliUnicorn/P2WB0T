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

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.User;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.UserUtil;

import java.util.List;

@Data
@AllArgsConstructor
public class Kara {
    public Kara() {}

    private int karaId;
    private String karanyId;
    private String mcNick = "-";
    private String admId;
    private String powod;
    private Long timestamp;
    private KaryEnum typKary;
    private Boolean aktywna = true;
    private String messageUrl = null;

    // Temp(mute|ban)
    private Long end;
    private String duration;

    // przy nadawaniu puna
    private Boolean punAktywna = false;

    public static String check(CommandContext context, User karany) {
        if (UserUtil.getPermLevel(context.getMember()).getNumer() <= UserUtil.getPermLevel(karany).getNumer()) {
            return context.getTranslate("check.hierarchy");
        }
        if (context.getUser().getId().equals(karany.getId())) return context.getTranslate("check.urself");
        if (karany.getId().equals(Ustawienia.instance.bot.botId)) return context.getTranslate("check.botaction");
        return null;
    }

    public static synchronized void put(CaseDao caseDao, Kara kara, ModLog modLog) {
        CaseConfig cc = new CaseConfig();
        int lastId = getNextId(caseDao.getAll());
        cc.setId(Integer.toString(lastId));
        kara.setKaraId(lastId);
        cc.setKara(kara);
        caseDao.save(cc);
        modLog.sendModlog(kara);
    }

    public static synchronized int getNextId(List<CaseConfig> cc) {
        int lastId = 0;
        for (CaseConfig aCase : cc) {
            lastId = Math.max(aCase.getKara().getKaraId(), lastId);
        }
        return lastId + 1;
    }

}