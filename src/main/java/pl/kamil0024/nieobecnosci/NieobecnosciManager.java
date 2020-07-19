package pl.kamil0024.nieobecnosci;

import com.google.inject.Inject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.bdate.Timespan;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.system.CytujCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.database.NieobecnosciDao;
import pl.kamil0024.core.database.config.NieobecnosciConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NieobecnosciManager {

    @Inject private ShardManager api;
    @Inject private NieobecnosciDao nieobecnosciDao;

    public NieobecnosciManager(ShardManager api, NieobecnosciDao nieobecnosciDao) {
        this.api = api;
        this.nieobecnosciDao = nieobecnosciDao;

        ScheduledExecutorService executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::xd, 0, 30, TimeUnit.SECONDS);
    }

    public synchronized void put(Message msg, long start, String powod, long end) {
        if (powod.isEmpty()) return;

        Nieobecnosc nb = new Nieobecnosc();
        nb.setUserId(msg.getAuthor().getId());
        nb.setId(nieobecnosciDao.getNextId(msg.getAuthor().getId()));
        nb.setStart(start);
        nb.setPowod(powod);
        nb.setEnd(end);
        nb.setAktywna(true);

        TextChannel txt = msg.getGuild().getTextChannelById(Ustawienia.instance.channel.nieobecnosci);
        if (txt == null) {
            Log.newError("Kanał do nieobecnosci jest nullem!");
            return;
        }

        MessageEmbed eb = getEmbed(nb, msg.getMember()).build();

        Message botmsg = txt.sendMessage(eb).complete();
        botmsg.addReaction(Objects.requireNonNull(CommandExecute.getReaction(msg.getAuthor(), false))).queue();

        nb.setMsgId(botmsg.getId());

        NieobecnosciConfig xd = nieobecnosciDao.get(msg.getAuthor().getId());
        xd.getNieobecnosc().add(nb);
        nieobecnosciDao.save(xd);
        msg.delete().queue();
    }

    public EmbedBuilder getEmbed(Nieobecnosc nieobecnosc, Member member) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(UserUtil.getColor(member));
        eb.setAuthor(UserUtil.getMcNick(member), null, member.getUser().getAvatarUrl());
        eb.setThumbnail(member.getUser().getAvatarUrl());

        eb.addField("Osoba zgłaszająca", UserUtil.getFullNameMc(member), false);
        eb.addField("Powód", nieobecnosc.getPowod(), false);
        eb.addField("Czas rozpoczęcia", sdf.format(new Date(nieobecnosc.getStart())), false);
        eb.addField("Powód", nieobecnosc.getPowod(), false);
        eb.addField("Czas zakończenia", sdf.format(new Date(nieobecnosc.getEnd())), false);
        eb.addField("Pozostało", new Timespan(new Date().getTime(), ModLog.getLang()).difference(nieobecnosc.getEnd()), false);
        eb.setFooter("ID: " + nieobecnosc.getId() + " | Ostatnia aktualizacja:");
        eb.setTimestamp(Instant.now());

        return eb;
    }

    public void xd() { update(); }

    public synchronized void update() {
        TextChannel txt = api.getTextChannelById(Ustawienia.instance.channel.nieobecnosci);

        if (txt == null) {
            Log.newError("Kanał do nieobecnosci jest nullem");
            return;
        }

        for (Nieobecnosc nb : nieobecnosciDao.getAllAktywne()) {
            long now = new Date().getTime();
            Message msg = CytujCommand.kurwaJDA(txt, nb.getMsgId());
            Member mem = Objects.requireNonNull(api.getGuildById(Ustawienia.instance.bot.guildId)).retrieveMemberById(nb.getUserId()).complete();
            if (mem == null) {
                Log.newError("Jezu " + nb.getUserId() + " wyszedł z serwera i nie mogę zaaktualizować nieobecności");
                continue;
            }

            if (msg == null) {
                Log.newError("Nieobecnosc o ID " + nb.getId() + " nie ma wiadomosci!");
                continue;
            }

            Log.debug("Nieobecnosci");
            Log.debug(nb.getEnd() + "");
            Log.debug(now + "");
            if (nb.getEnd() - now <= 0) {
                try {
                    NieobecnosciConfig nbc = nieobecnosciDao.get(nb.getUserId());
                    msg.delete().queue();
                    nbc.getNieobecnosc().remove(nb);
                    nb.setAktywna(false);
                    nbc.getNieobecnosc().add(nb);
                    nieobecnosciDao.save(nbc);
                    PrivateChannel pv = mem.getUser().openPrivateChannel().complete();
                    pv.sendMessage("Twój urlop właśnie się zakończył!").complete();
                } catch (Exception ignored) {}
                continue;
            }
            msg.editMessage(getEmbed(nb, mem).build()).queue();
        }
    }
}
