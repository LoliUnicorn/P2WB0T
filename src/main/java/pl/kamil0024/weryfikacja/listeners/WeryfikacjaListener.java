package pl.kamil0024.weryfikacja.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.database.config.DiscordInviteConfig;
import pl.kamil0024.core.database.config.MultiConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.Nick;
import pl.kamil0024.core.util.UserUtil;

import javax.annotation.Nonnull;

public class WeryfikacjaListener extends ListenerAdapter {

    private final APIModule apiModule;
    private final MultiDao multiDao;

    public WeryfikacjaListener(APIModule apiModule, MultiDao multiDao) {
        this.apiModule = apiModule;
        this.multiDao = multiDao;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!event.getChannel().getId().equals("740157959207780362") || event.getAuthor().isBot() || event.getAuthor().isFake()) return;

        String msg = event.getMessage().getContentRaw();
        if (msg.isEmpty()) return;

        DiscordInviteConfig dc = apiModule.getDiscordConfig(msg);
        if (dc == null) {
            return;
        }
        Role ranga = null;

        if (dc.getRanga().equals("gracz")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.gracz); }
        if (dc.getRanga().equals("vip")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vip); }
        if (dc.getRanga().equals("vipplus")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.vipplus); }
        if (dc.getRanga().equals("mvp")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvp); }
        if (dc.getRanga().equals("mvpplus")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplus); }
        if (dc.getRanga().equals("mvpplusplus")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.mvpplusplus); }
        if (dc.getRanga().equals("sponsor")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.sponsor); }
        if (dc.getRanga().equals("miniyt")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.miniyt); }
        if (dc.getRanga().equals("yt")) { ranga = event.getGuild().getRoleById(Ustawienia.instance.rangi.yt); }

        if (ranga == null) {
            return;
        }

        String nickname = "[" + ranga.getName().toUpperCase() + "]";

        if (ranga.getName().toLowerCase().equals("youtuber")) { nickname = "[YT]"; }
        if (ranga.getName().toLowerCase().equals("miniyt")) { nickname = "[MiniYT]"; }
        if (ranga.getName().toLowerCase().equals("gracz")) { nickname = ""; }

        Member mem = event.getMember();
        if (mem != null) {
            try {
                event.getGuild().modifyNickname(event.getMember(), nickname).complete();
            } catch (Exception ignored) {}
            event.getGuild().addRoleToMember(event.getMember(), ranga).complete();

            Member member = event.getGuild().retrieveMemberById(event.getMember().getId()).complete();
            String mc = UserUtil.getMcNick(member, true);

            MultiConfig conf = multiDao.get(event.getAuthor().getId());
            conf.getNicki().add(new Nick(mc, new BDate().getTimestamp()));
            multiDao.save(conf);
        }

        apiModule.getDcCache().invalidate(dc.getKod());

    }

}
