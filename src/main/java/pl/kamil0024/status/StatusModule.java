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

package pl.kamil0024.status;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.status.listeners.ChangeNickname;
import pl.kamil0024.status.statusy.CheckMute;
import pl.kamil0024.status.statusy.CheckUnmute;

import java.util.Timer;

public class StatusModule implements Modul {

    private boolean start = false;
    private ChangeNickname changeNickname;

    private final ShardManager api;


    public StatusModule(ShardManager api) {
        this.api = api;
    }

    @Override
    public boolean startUp() {
        this.changeNickname = new ChangeNickname();
        Timer tim = new Timer();
        tim.schedule(new CheckMute(api), 0, 120000); // 300000
        tim.schedule(new CheckUnmute(api), 0, 120000); // 180000
        api.addEventListener(changeNickname);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(changeNickname);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        start = bol;
    }
}
