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

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kamil0024.commands.moderation.DowodCommand;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.kary.Dowod;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class DowodWaiter {

    private final Logger logger = LoggerFactory.getLogger(DowodWaiter.class);

    private final String userId;
    private final CaseConfig cc;
    private final CaseDao cd;
    private final TextChannel channel;
    private final EventWaiter eventWaiter;

    private Message botMsg;

    public void start() {
        logger.debug("Tworze waitera dla: " + userId);
        logger.debug("Jego kanał to: " + channel.getId());
        botMsg = channel.sendMessage(String.format("<@%s>, zapisz dowód... (jeżeli takowego nie ma, napisz `anuluj`)", userId)).complete();
        waitForMessage();
    }

    private void waitForMessage() {
        try {
            eventWaiter.waitForEvent(MessageReceivedEvent.class, this::checkMessage,
                    this::event, 40, TimeUnit.SECONDS, this::clear);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkMessage(MessageReceivedEvent e) {
        logger.debug("checkMessage: 1");
        if (!e.getAuthor().getId().equals(userId)) return false;
        logger.debug("checkMessage: 2");
        if (e.getMessage().getContentRaw().equalsIgnoreCase("anuluj")) {
            logger.debug("checkMessage: 3");
            clear();
            return false;
        }
        logger.debug("checkMessage: 4");
        return e.isFromGuild() && e.getTextChannel().getId().equals(channel.getId()) && e.getAuthor().getId().equals(userId);
    }

    private void clear() {
        try {
            botMsg.delete().queue(c -> {});
        } catch (Exception ignored) { }
    }

    private void event(MessageReceivedEvent e) {
        try {
            logger.debug("Wykonuje event dla: " + e.getAuthor().getId());
            logger.debug("Jego kanal to: " + e.getChannel().getId());
            logger.debug("event: 1");
            Message msg = e.getTextChannel().retrieveMessageById(e.getMessageId()).complete();
            Dowod d = DowodCommand.getKaraConfig(msg.getContentRaw(), msg);
            if (d == null) {
                logger.debug("event: 2");
                e.getTextChannel().sendMessage("Dowód jest pusty?").queue();
                return;
            }
            logger.debug("event: 3");
            if (cc.getKara().getDowody() == null) cc.getKara().setDowody(new ArrayList<>());
            cc.getKara().getDowody().add(d);
            e.getTextChannel().sendMessage("Pomyślnie zapisano dowód!").queue();
            cd.save(cc);
            clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}