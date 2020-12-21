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

package pl.kamil0024.weryfikacja.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.database.config.DiscordInviteConfig;
import pl.kamil0024.core.database.config.MultiConfig;
import pl.kamil0024.core.util.Nick;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class WeryfikacjaListener extends ListenerAdapter {

    private final APIModule apiModule;
    private final MultiDao multiDao;
    private final ModLog modLog;
    private final CaseDao caseDao;

    public WeryfikacjaListener(APIModule apiModule, MultiDao multiDao, ModLog modLog, CaseDao caseDao) {
        this.apiModule = apiModule;
        this.multiDao = multiDao;
        this.modLog = modLog;
        this.caseDao = caseDao;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals("740157959207780362") || event.getAuthor().isBot() || !event.isFromGuild()) return;

        String msg = event.getMessage().getContentRaw();
        try {
            event.getMessage().delete().complete();
        } catch (Exception ignored) { }

        DiscordInviteConfig dc = apiModule.getDiscordConfig(msg);
        if (dc == null) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", podałeś zły kod! Sprawdź swój kod jeszcze raz na serwerze lub wygeneruj nowy.")
                    .queue(m -> m.delete().queueAfter(11, TimeUnit.SECONDS));
            return;
        }
        Role ranga = null;

        switch (dc.getRanga()) {
            case "Gracz":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.gracz);
                break;
            case "VIP":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vip);
                break;
            case "VIP+":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vipplus);
                break;
            case "MVP":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvp);
                break;
            case "MVP+":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplus);
                break;
            case "MVP++":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplusplus);
                break;
            case "Sponsor":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.sponsor);
                break;
            case "MiniYT":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.miniyt);
                break;
            case "YouTuber":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.yt);
                break;
            case "Pomocnik":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.pomocnik);
                break;
            case "Stażysta":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.stazysta);
                break;
            case "Build_Team":
                ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.buildteam);
        }

        if (ranga == null) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", twoja ranga została źle wpisana! Skontaktuj się z kimś z administracji")
                    .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
            return;
        }

        String nickname = "[" + ranga.getName().toUpperCase() + "]";

        switch (ranga.getName().toLowerCase()) {
            case "youtuber":
                nickname = "[YT]";
                break;
            case "miniyt":
                nickname = "[MiniYT]";
                break;
            case "gracz":
                nickname = "";
                break;
            case "pomocnik":
                nickname = "[POM]";
                break;
            case "stażysta":
                nickname = "[STAŻ]";
                break;
            case "team team":
                nickname = "[BUILD TEAM]";
                break;
        }

        Member mem = event.getMember();
        if (mem != null) {
            try {
                event.getGuild().modifyNickname(mem, nickname + " " + dc.getNick()).complete();
            } catch (Exception ignored) {}
            event.getGuild().addRoleToMember(mem, ranga).complete();

            MultiConfig conf = multiDao.get(event.getAuthor().getId());
            conf.getNicki().add(new Nick(nickname + " " + dc.getNick(), new BDate().getTimestamp()));
            multiDao.save(conf);

            modLog.checkKara(event.getMember(), true,
                    caseDao.getNickAktywne(dc.getNick().replace(" ", "")));

            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", pomyślnie zweryfikowano. Witamy na serwerze sieci P2W!")
                    .allowedMentions(Collections.singleton(Message.MentionType.USER))
                    .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
            if (ranga.getId().equalsIgnoreCase(Ustawienia.instance.rangi.gracz)) {
                CheckMk mk = new CheckMk(event.getMember());
                mk.check();
            }

        }
        apiModule.getDcCache().invalidate(dc.getKod());
    }

}
