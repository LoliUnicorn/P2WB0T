package pl.kamil0024.weryfikacja.listeners;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.config.DiscordInviteConfig;
import pl.kamil0024.core.logger.Log;

import javax.annotation.Nonnull;

public class WeryfikacjaListener extends ListenerAdapter {

    private final APIModule apiModule;

    public WeryfikacjaListener(APIModule apiModule) {
        this.apiModule = apiModule;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!event.getChannel().getId().equals("740157959207780362") || event.getAuthor().isBot() || event.getAuthor().isFake()) return;

        String msg = event.getMessage().getContentRaw();
        if (msg.isEmpty()) return;

        DiscordInviteConfig dc = apiModule.getDiscordConfig(msg);
        if (dc == null) {
            Log.debug("kod jest nieprawidlowy");
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
            Log.debug("ranga jest nullem");
            return;
        }

        String nickname = "[" + ranga.getName() + "]";

        if (ranga.getName().toLowerCase().equals("youtuber")) { nickname = "[YT]"; }

        if (ranga.getName().toLowerCase().equals("miniyt")) { nickname = "[MiniYT]"; }

        event.getChannel().sendMessage("daje range " + ranga.getName() + " i zmieniam nick na `"
                + nickname + " " + dc.getNick() + "`").queue();

        apiModule.getDcCache().invalidate(dc.getKod());

    }

}
