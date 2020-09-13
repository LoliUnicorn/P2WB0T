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

package pl.kamil0024.youtrack.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.DynamicEmbedPageinator;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.youtrack.YouTrack;
import pl.kamil0024.youtrack.exceptions.APIException;
import pl.kamil0024.youtrack.listener.MessageListener;
import pl.kamil0024.youtrack.models.Issue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

public class IssuesCommand extends Command {

    private final EventWaiter eventWaiter;
    private final YouTrack youTrack;

    public IssuesCommand(EventWaiter eventWaiter, YouTrack youTrack) {
        name = "issues";
        aliases.add("iss");
        permLevel = PermLevel.HELPER;
        this.eventWaiter = eventWaiter;
        this.youTrack = youTrack;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        Message msg = context.send("Ładuje...").complete();
        Integer arg = context.getParsed().getNumber(context.getArgs().get(0));
        if (arg == null) arg = 0;
        DateTime dt = new DateTime().minusDays(arg);
        List<FutureTask<EmbedBuilder>> pages = new ArrayList<>();
        try {
            for (Issue issue : youTrack.getIssues()) {
                if (dt.isAfter(new DateTime(issue.getCreated()).minusDays(arg))) {
                    pages.add(new FutureTask<>(() -> MessageListener.generateEmbedBuilder(issue)));
                }
            }
        } catch (APIException e) {
            msg.editMessage("Nie udało się pobrać issuesów! " + e.getLocalizedMessage()).queue();
            return false;
        }
        if (pages.isEmpty()) {
            msg.editMessage("Nie ma żadnych issuesów!").queue();
            return false;
        }
        new DynamicEmbedPageinator(pages, context.getUser(), eventWaiter, context.getJDA(), 60)
                .create(msg);
        return true;
    }

}
