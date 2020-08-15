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

package pl.kamil0024.weryfikacja;

import pl.kamil0024.api.APIModule;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.status.listeners.ChangeNickname;
import pl.kamil0024.weryfikacja.listeners.WeryfikacjaListener;

public class WeryfikacjaModule implements Modul {

    private final APIModule apiModule;
    private final MultiDao multiDao;
    private final ModLog modLog;
    private final CaseDao caseDao;

    private boolean start = false;
    private WeryfikacjaListener weryfikacjaListener;
    private ChangeNickname changeNickname;

    public WeryfikacjaModule(APIModule apiModule, MultiDao multiDao, ModLog modLog, CaseDao caseDao) {
        this.apiModule = apiModule;
        this.multiDao = multiDao;
        this.modLog = modLog;
        this.caseDao = caseDao;
    }

    @Override
    public boolean startUp() {
        this.changeNickname = new ChangeNickname();
        this.weryfikacjaListener = new WeryfikacjaListener(apiModule, this.multiDao, modLog, caseDao);
        apiModule.getApi().addEventListener(weryfikacjaListener, changeNickname);
        return true;
    }

    @Override
    public boolean shutDown() {
        apiModule.getApi().removeEventListener(weryfikacjaListener, changeNickname);
        return true;
    }

    @Override
    public String getName() {
        return "weryfikacja";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

}
