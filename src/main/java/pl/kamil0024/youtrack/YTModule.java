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

package pl.kamil0024.youtrack;

import com.google.inject.Inject;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.youtrack.commands.IssuesCommand;
import pl.kamil0024.youtrack.impl.YouTrackImpl;
import pl.kamil0024.youtrack.listener.MessageListener;

import java.util.ArrayList;

public class YTModule implements Modul {

    private ArrayList<Command> cmd;

    @Inject CommandManager commandManager;
    @Inject ShardManager api;
    @Inject EventWaiter eventWaiter;

    private boolean start = false;
    private YouTrack youTrack;

    private MessageListener msgListener;

    public YTModule(CommandManager commandManager, ShardManager api, EventWaiter eventWaiter, YouTrack youTrack) {
        this.commandManager = commandManager;
        this.api = api;
        this.eventWaiter = eventWaiter;
        this.youTrack = youTrack;
    }

    @Override
    public boolean startUp() {
        this.msgListener = new MessageListener(youTrack);
        api.addEventListener(msgListener);

        cmd = new ArrayList<>();
        cmd.add(new IssuesCommand(eventWaiter, youTrack));
        cmd.forEach(commandManager::registerCommand);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        commandManager.unregisterCommands(cmd);
        api.removeEventListener(msgListener);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "youtrack";
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
