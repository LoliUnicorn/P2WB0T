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

package pl.kamil0024.chat.listener;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import pl.kamil0024.chat.Action;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.moderation.MuteCommand;
import pl.kamil0024.commands.moderation.PunishCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Dowod;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.stats.StatsModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

public class KaryListener extends ListenerAdapter {

    @Getter private static final ArrayList<Action> embedy = new ArrayList<>();

    private final KaryJSON karyJSON;
    private final CaseDao caseDao;
    private final ModLog modLog;
    private final StatsModule statsModule;

    public KaryListener(KaryJSON karyJSON, CaseDao caseDao, ModLog modLog, StatsModule statsModule) {
        this.karyJSON = karyJSON;
        this.caseDao = caseDao;
        this.modLog = modLog;
        this.statsModule = statsModule;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)
                || !event.getChannel().getId().equals(Ustawienia.instance.channel.moddc)) return;
        if (UserUtil.getPermLevel(event.getMember()).getNumer() == PermLevel.MEMBER.getNumer()) return;
        if (event.getMember().getUser().isBot()) return;

        check(event);
    }

    private synchronized void check(GuildMessageReactionAddEvent event) {
        try {
            for (Action entry : getEmbedy()) {
                if (!entry.getBotMsg().equals(event.getMessageId())) continue;

                Message msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
                if (event.getReactionEmote().getId().equals(Ustawienia.instance.emote.red)) {
                    deleteMessage(msg);
                    getEmbedy().remove(entry);
                    continue;
                }

                deleteMessage(entry.getMsg(), msg);
                if (event.getReactionEmote().getId().equals("623630774171729931")) {
                    getEmbedy().remove(entry);
                    continue;
                }
                Dowod d = new Dowod();
                d.setId(1);
                d.setUser(event.getMember().getId());
                d.setContent("Wystawione automatycznie. Treść wiadomości poniżej.\n\n" + MarkdownSanitizer.escape(entry.getMsg().getContentDisplay()));
                d.setImage(null);

                Member mem = null;
                try {
                    mem = event.getGuild().retrieveMemberById(entry.getMsg().getAuthor().getId()).complete();
                } catch (Exception ignored) { }

                if (mem == null) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", użytkownik wyszedł z serwera??")
                            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    continue;
                }
                if (MuteCommand.hasMute(mem)) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", użytkownik jest wyciszony!")
                            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    continue;
                }

                KaryJSON.Kara kara = karyJSON.getByName(entry.getKara().getPowod());
                if (kara == null) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", kara `" + entry.getKara().getPowod() + "` jest źle wpisana!").queue();
                    continue;
                }

                PunishCommand.putPun(kara, Collections.singletonList(mem), event.getMember(), event.getChannel(), caseDao, modLog, statsModule, d, null);
                getEmbedy().remove(entry);
            }

        } catch (ConcurrentModificationException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMessage(Message... m) {
        for (Message message : m) {
            try {
                message.delete().queue(s -> {});
            } catch (Exception ignored) { }
        }
    }

}
