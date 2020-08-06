package pl.kamil0024.weryfikacja.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!event.getChannel().getId().equals("740157959207780362") || event.getAuthor().isBot() || event.getAuthor().isFake()) return;

        String msg = event.getMessage().getContentRaw();
        event.getMessage().delete().complete();

        DiscordInviteConfig dc = apiModule.getDiscordConfig(msg);
        if (dc == null) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", podałeś zły kod! Sprawdź swój kod jeszcze raz na serwerze lub wygeneruj nowy.")
                    .queue(m -> m.delete().queueAfter(11, TimeUnit.SECONDS));
            return;
        }
        Role ranga = null;

        if (dc.getRanga().equals("Gracz")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.gracz); }
        if (dc.getRanga().equals("VIP")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vip); }
        if (dc.getRanga().equals("VIP+")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vipplus); }
        if (dc.getRanga().equals("MVP")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvp); }
        if (dc.getRanga().equals("MVP+")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplus); }
        if (dc.getRanga().equals("MVP++")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplusplus); }
        if (dc.getRanga().equals("Sponsor")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.sponsor); }
        if (dc.getRanga().equals("MiniYT")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.miniyt); }
        if (dc.getRanga().equals("YouTuber")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.yt); }
        if (dc.getRanga().equals("Pomocnik")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.pomocnik); }

        if (ranga == null) {
            event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", twoja ranga została źle wpisana! Skontaktuj się z kimś z administracji")
                    .queue(m -> m.delete().queueAfter(8, TimeUnit.SECONDS));
            return;
        }

        String nickname = "[" + ranga.getName().toUpperCase() + "]";

        if (ranga.getName().toLowerCase().equals("youtuber")) { nickname = "[YT]"; }
        if (ranga.getName().toLowerCase().equals("miniyt")) { nickname = "[MiniYT]"; }
        if (ranga.getName().toLowerCase().equals("gracz")) { nickname = ""; }
        if (ranga.getName().toLowerCase().equals("pomocnik")) { nickname = "[POM]"; }

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
        }
        apiModule.getDcCache().invalidate(dc.getKod());
    }

}
