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

package pl.kamil0024.core.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import pl.kamil0024.commands.moderation.DowodCommand;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.kary.Dowod;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DowodWaiter {

    public EventWaiter eventWaiter;
    public CaseConfig cc;
    public CaseDao cd;
    public String userId;
    public TextChannel channel;
    private Message botMsg;

    public DowodWaiter(String userId, CaseConfig cc, CaseDao cd, TextChannel channel, EventWaiter eventWaiter) {
        this.eventWaiter = eventWaiter;
        this.userId = userId;
        this.cc = cc;
        this.cd = cd;
        this.channel = channel;
    }

    public void start() {
        botMsg = channel.sendMessage(String.format("<@%s>, zapisz dowód... (jeżeli takowego nie ma, napisz `anuluj`)", userId)).complete();
        waitForMessage();
    }

    private void waitForMessage() {
        eventWaiter.waitForEvent(MessageReceivedEvent.class, this::checkMessage,
                this::event, 40, TimeUnit.SECONDS, this::clear);
    }

    private boolean checkMessage(MessageReceivedEvent e) {
        Log.debug("checkMessage: 1");
        if (!e.getAuthor().getId().equals(userId)) return false;
        Log.debug("checkMessage: 2");
        if (e.getMessage().getContentRaw().equalsIgnoreCase("anuluj")) {
            Log.debug("checkMessage: 3");
            clear();
            return false;
        }
        Log.debug("checkMessage: 4");
        return e.isFromGuild() && e.getTextChannel().getId().equals(channel.getId());
    }

    private void clear() {
        try {
            botMsg.delete().queue(c -> {});
        } catch (Exception ignored) { }
    }

    private void event(MessageReceivedEvent e) {
        try {
            Log.debug("event: 1");Syn
            Message msg = e.getTextChannel().retrieveMessageById(e.getMessageId()).complete();
            Log.debug("event: 2");
            Dowod d = DowodCommand.getKaraConfig(msg.getContentRaw(), msg);
            Log.debug("event: 3");
            if (d == null) {
                Log.debug("event: 4");
                e.getTextChannel().sendMessage("Dowód jest pusty?").queue();
                return;
            }
            Log.debug("event: 5");
            if (cc.getKara().getDowody() == null) cc.getKara().setDowody(new ArrayList<>());
            cc.getKara().getDowody().add(d);
            Log.debug("event: 6");
            e.getTextChannel().sendMessage("Pomyślnie zapisano dowód!").queue();
            Log.debug("event: 7");
            cd.save(cc);
            Log.debug("event: 8");
            clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}