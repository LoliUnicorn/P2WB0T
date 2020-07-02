package pl.kamil0024.nieobecnosci.listeners;

import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.commands.system.CytujCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.NieobecnosciDao;
import pl.kamil0024.core.database.config.NieobecnosciConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.nieobecnosci.NieobecnosciManager;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class NieobecnosciListener extends ListenerAdapter {

    @Inject private ShardManager api;
    @Inject private NieobecnosciDao nieobecnosciDao;
    @Inject private NieobecnosciManager nieobecnosciManager;

    public NieobecnosciListener(ShardManager api, NieobecnosciDao nieobecnosciDao, NieobecnosciManager nieobecnosciManager) {
        this.api = api;
        this.nieobecnosciDao = nieobecnosciDao;
        this.nieobecnosciManager = nieobecnosciManager;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        if (!e.getChannel().getId().equals(Ustawienia.instance.channel.nieobecnosci) || e.getAuthor().isBot()) return;

        String[] msg = e.getMessage().getContentRaw().split("\n");
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        String log = "Chciano dać urlop dla " + e.getAuthor().getId() + " ale ";
        String powod = null;
        long start = 0, end = 0;

        NieobecnosciConfig nbc = nieobecnosciDao.get(e.getAuthor().getId());
        if (e.getMessage().getContentRaw().contains("Przedłużam:")) {
            Nieobecnosc xd = null;
            for (Nieobecnosc nieobecnosc : nbc.getNieobecnosc()) {
                assert nieobecnosc.isAktywna();
                xd = nieobecnosc;
                nbc.getNieobecnosc().remove(nieobecnosc);
                break;
            }
            if (xd == null) {
                e.getChannel().sendMessage(e.getAuthor().getAsMention() + " nie masz aktywnej nieobecnosci").
                        queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }

            try {
                xd.setEnd(sfd.parse(e.getMessage().getContentRaw().split("Przedłużam: ")[1]).getTime());
                nbc.getNieobecnosc().add(xd);
                nieobecnosciDao.save(nbc);
                nieobecnosciManager.update();
                e.getMessage().delete().queue();
            } catch (Exception takkk) {
                Log.debug(log + "parser przedłużania jest zły");
            }
            return;
        }

        // msg[0] - data rozpoczęcia
        // msg[1] - powód
        // msg[2] - data zakończenia

        try {
            start = sfd.parse(msg[0].split(": ")[1]).getTime();
            powod = msg[1].split(": ")[1];
            end = sfd.parse(msg[2].split(": ")[1]).getTime();
        } catch (Exception xd) {
            Log.debug(log + "parser jest zły");
            return;
        }

        for (Nieobecnosc nieobecnosc : nbc.getNieobecnosc()) {
            if (nieobecnosc.isAktywna()) {
                e.getChannel().sendMessage(e.getAuthor().getAsMention() + " masz jeszcze niezakończoną nieobecność o ID" + nieobecnosc.getId() + "!").
                        queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                return;
            }
        }

        if (start >= end) {
            e.getChannel().sendMessage(e.getAuthor().getAsMention() + " data zakończenia jest wcześniejsza od daty rozpoczęcia").
                    queue( m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        nieobecnosciManager.put(e.getMessage(), start, powod, end);
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent e) {
        synchronized (e.getGuild().getId()) {
            if (!e.getChannel().getId().equals(Ustawienia.instance.channel.nieobecnosci)) return;

            if (e.getReactionEmote().isEmoji() || !e.getReactionEmote().getEmote().getId().equals(Ustawienia.instance.emote.red)) return;

            for (Nieobecnosc nieobecnosc : nieobecnosciDao.getAllAktywne()) {
                if (nieobecnosc.getMsgId().equals(e.getMessageId())) {
                    if (nieobecnosc.getUserId().equals(e.getMember().getId()) || UserUtil.getPermLevel(e.getMember()).getNumer() >= PermLevel.ADMINISTRATOR.getNumer()) {
                        NieobecnosciConfig nbc = nieobecnosciDao.get(nieobecnosc.getUserId());
                        nbc.getNieobecnosc().remove(nieobecnosc);
                        nieobecnosc.setAktywna(false);
                        nbc.getNieobecnosc().add(nieobecnosc);
                        nieobecnosciDao.save(nbc);
                        Message msg = CytujCommand.kurwaJDA(e.getChannel(), e.getMessageId());
                        if (msg == null) continue;
                        msg.delete().queue();
                        break;
                    }
                }
            }
        }
    }
}
