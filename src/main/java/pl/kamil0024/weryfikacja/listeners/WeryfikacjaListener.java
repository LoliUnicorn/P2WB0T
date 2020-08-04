package pl.kamil0024.weryfikacja.listeners;

import com.google.gson.Gson;
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
        Log.debug(new Gson().toJson(dc));
        if (dc == null) {
            Log.debug("kod jest nullem");
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

        if (ranga == null) {
            Log.debug("ranga jest zla");
            return;
        }

        String nickname = "[" + ranga.getName().toUpperCase() + "]";

        if (ranga.getName().toLowerCase().equals("youtuber")) { nickname = "[YT]"; }
        if (ranga.getName().toLowerCase().equals("miniyt")) { nickname = "[MiniYT]"; }
        if (ranga.getName().toLowerCase().equals("gracz")) { nickname = ""; }

        Member mem = event.getMember();
        Log.debug("tak");
        if (mem != null) {
            Log.debug("jest git wszystko");
            try {
                event.getGuild().modifyNickname(mem, nickname + " " + dc.getNick()).complete();
            } catch (Exception ignored) {}
            event.getGuild().addRoleToMember(mem, ranga).complete();

            MultiConfig conf = multiDao.get(event.getAuthor().getId());
            conf.getNicki().add(new Nick(nickname + " " + dc.getNick(), new BDate().getTimestamp()));
            multiDao.save(conf);
        }

        apiModule.getDcCache().invalidate(dc.getKod());

    }

}
