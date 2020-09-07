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

package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.dews.ShellCommand;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;

import java.time.temporal.ChronoUnit;

public class PingCommand extends Command {

    public PingCommand() {
        name = "ping";
        cooldown = 15;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        double derpmc = 0, feerko = 0, roizy = 0;
        String out = ShellCommand.shell("ping derpmc.pl -c 1 && ping feerko.pl -c 1 && ping roizy.pl -c 1");
        if (out != null) {
            for (String s : out.split("\n")) {
                if (s.contains("time=")) {
                    String ms = s.split("time=")[1].replaceAll(" ms", "");
                    if (derpmc == 0) derpmc = Double.parseDouble(ms);
                    else if (feerko == 0) feerko = Double.parseDouble(ms);
                    else if (roizy == 0) roizy = Double.parseDouble(ms);
                }
            }
        }
        Message msg = context.send(context.getTranslate("ping.ping")).complete();
        long ping = context.getEvent().getMessage().getTimeCreated().until(msg.getTimeCreated(), ChronoUnit.MILLIS);
        msg.editMessage(context.getTranslate("ping.pong", ping, context.getEvent().getJDA().getGatewayPing(), derpmc, feerko, roizy)).queue();
        return true;
    }

}

