package pl.kamil0024.commands;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.bdate.util.BLanguage;
import pl.kamil0024.commands.moderation.MuteCommand;
import pl.kamil0024.commands.moderation.TempbanCommand;
import pl.kamil0024.commands.moderation.TempmuteCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.BetterStringBuilder;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ModLog extends ListenerAdapter {

    private final ShardManager api;
    private final TextChannel modlog;
    private CaseDao caseDao;

    private ScheduledExecutorService executorSche;

    public ModLog(ShardManager api, CaseDao caseDao) {
        this.api = api;
        this.modlog = api.getTextChannelById(Ustawienia.instance.channel.modlog);
        if (modlog == null) {
            Log.newError("Kanał do modlogów jest nullem");
            throw new UnsupportedOperationException("Kanał do modlogów jest nullem");
        }
        this.caseDao = caseDao;
        executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::tak, 0, 2, TimeUnit.MINUTES);
    }

    @SneakyThrows
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;

        Thread.sleep(10000);

        List<CaseConfig> cc = caseDao.getAktywe(event.getUser().getId());
        String nick = UserUtil.getMcNick(event.getMember());

        checkKara(event, false, cc);
//        checkKara(event, true, caseDao.getNickAktywne(nick));
    }

    private synchronized void checkKara(GuildMemberJoinEvent event, boolean nick, List<CaseConfig> cc) {
        Member user = event.getMember();
        for (CaseConfig config : cc) {
            Kara k = config.getKara();
            String powod = null;

            switch (k.getTypKary()) {
                case BAN:
                    powod = "Te konto jest permanentnie zbanowane!";
                    event.getGuild().ban(user, 0, powod).queue();
                    break;
                case MUTE:
                    powod = "Te konto jest permanentnie wyciszone";
                    event.getGuild().addRoleToMember(user, Objects.requireNonNull(api.getRoleById(Ustawienia.instance.muteRole))).queue();
                    break;
                case TEMPBAN:
                    powod = "Te konto jest tymczasowo zbanowane";
                    TempbanCommand.tempban(user, api.retrieveUserById(config.getKara().getAdmId()).complete(), k.getPowod(), k.getDuration(), caseDao, this, true);
                    break;
                case TEMPMUTE:
                    powod = "Te konto jest tymczasowo wyciszone";
                    TempmuteCommand.tempmute(user, api.retrieveUserById(config.getKara().getAdmId()).complete(), k.getPowod(), k.getDuration(), caseDao, this, true);
                    break;
            }

            if (powod == null) continue;

            Kara kara = new Kara();

            kara.setKaranyId(user.getId());
            kara.setMcNick(UserUtil.getMcNick(user));
            kara.setAdmId(k.getAdmId());
            kara.setPowod(powod + " (" + k.getPowod() + ") [ID: " + k.getKaraId() + "]");
            kara.setTimestamp(new Date().getTime());
            kara.setTypKary(k.getTypKary());
            kara.setAktywna(false);

            try {
                kara.setEnd(config.getKara().getEnd());
                kara.setDuration(config.getKara().getDuration());
            } catch (Exception ignored) { }

            //noinspection ConstantConditions
            if (!powod.contains("Te konto jest") && kara.getKaraId() != 0) {
                if (nick) {
                    kara.setAktywna(true);
                    CaseConfig caseconfig = new CaseConfig(user.getId());
                    caseconfig.setKara(kara);
                    caseDao.save(caseconfig);

                    for (CaseConfig ccase : caseDao.getAktywe(user.getId())) {
                        ccase.getKara().setAktywna(false);
                            caseDao.save(ccase);
                    }
                }
                sendModlog(kara, true);
                return;
            }
        }
    }

    private void tak() {
        check();
    } // inaczej executorSche nie działa lol

    private synchronized void check() {
        Date data = new Date();
        Guild g = api.getGuildById(Ustawienia.instance.bot.guildId);
        Role muteRole = api.getRoleById(Ustawienia.instance.muteRole);
        if (muteRole == null) throw new NullPointerException("muteRole jest nullem");
        if (g == null) throw new NullPointerException("Serwer docelowy jest nullem");
        List<CaseConfig> cc = caseDao.getAllAktywne();
        for (CaseConfig a : cc) {
            Kara aCase = a.getKara();
            if (!aCase.getAktywna()) continue;

            KaryEnum typ = aCase.getTypKary();
            if (typ == KaryEnum.TEMPBAN || typ == KaryEnum.TEMPMUTE) {
                Long end = aCase.getEnd();
                if (end == null) {
                    Log.newError("Kara jest TEMPBAN || TEMPMUTE a getEnd() jest nullem ID:" + aCase.getKaraId());
                    continue;
                }
                if (end - data.getTime() <= 0) {
                    try {
                        User u = api.retrieveUserById(aCase.getKaranyId()).complete();
                        if (u == null) continue;
                        Member m = null;
                        try {
                            m = g.retrieveMember(u).complete();
                        } catch (ErrorResponseException ignored) {}

                        if (typ == KaryEnum.TEMPBAN) {
                            g.unban(aCase.getKaranyId()).complete();
                        }
                        if (typ == KaryEnum.TEMPMUTE) {
                            if (m != null) {
                                g.removeRoleFromMember(m, muteRole).complete();
                            }
                        }

                        Kara kara = new Kara();
                        kara.setKaranyId(aCase.getKaranyId());
                        if (m != null) kara.setMcNick(UserUtil.getMcNick(m));
                        kara.setAdmId(Ustawienia.instance.bot.botId);
                        kara.setPowod("Czas minął. (ID: " + aCase.getKaraId() + ")");
                        kara.setTimestamp(data.getTime());
                        kara.setTypKary(typ == KaryEnum.TEMPBAN ? KaryEnum.UNBAN : KaryEnum.UNMUTE);
                        kara.setKaraId(Kara.getNextId(cc));
                        aCase.setAktywna(false);
                        sendModlog(kara);
                        CaseConfig kurwa = new CaseConfig(String.valueOf(aCase.getKaraId()));
                        kurwa.setKara(aCase);
                        caseDao.save(kurwa);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "Nie udało się dać kary %s dla %s (ID: %s) bo: %s";
                        Log.newError(String.format(msg, typ == KaryEnum.TEMPBAN ? KaryEnum.UNBAN : KaryEnum.UNMUTE, aCase.getKaranyId(), aCase.getKaraId(), e.getMessage()));
                    }
                    if (typ == KaryEnum.TEMPMUTE) {
                        User u = api.retrieveUserById(aCase.getKaranyId()).complete();
                        if (u != null) {
                            Member m = g.retrieveMember(u).complete();
                            if (MuteCommand.hasMute(m)) {
                                Log.newError("Uzytkownik " + UserUtil.getFullName(u) + " dostal unmuta, ale nadal ma range Wyciszony!");
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized void sendModlog(Kara kara) {
        sendModlog(kara, true);
    }

    public synchronized void sendModlog(Kara kara, boolean sendDm) {
        Message msg = modlog.sendMessage(getEmbed(kara, api, sendDm).build()).complete();
        String url = msg.getJumpUrl().replaceAll("https://discord(app)?.com/channels/", "");

        CaseConfig cc = caseDao.get(kara.getKaraId());
        if (cc.getKara() != null) {
            cc.getKara().setMessageUrl(url);
            caseDao.save(cc);
        }
    }

    public static EmbedBuilder getEmbed(Kara kara, ShardManager api) {
        return getEmbed(kara, api, false);
    }

    @SuppressWarnings("ConstantConditions")
    private static EmbedBuilder getEmbed(Kara kara, ShardManager api, Boolean bol) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy `@` HH:mm:ss");
        EmbedBuilder eb = new EmbedBuilder();
        User u = api.retrieveUserById(kara.getKaranyId()).complete();
        Member mem = api.getGuildById(Ustawienia.instance.bot.guildId).retrieveMemberById(kara.getAdmId()).complete();
        if (mem != null) eb.setColor(UserUtil.getColor(mem));
        eb.addField("Osoba karana", UserUtil.getFullName(u), false);
        eb.addField("Nick w mc", kara.getMcNick(), false);
        eb.addField("Administrator", UserUtil.getFullName(mem.getUser()), false);
        eb.addField("Powód", kara.getPowod(), false);
        if (u.getAvatarUrl() != null) eb.setThumbnail(u.getAvatarUrl());
        eb.addField("Nadano o", sfd.format(new Date(kara.getTimestamp())), false);
        if (kara.getTypKary() == KaryEnum.TEMPBAN || kara.getTypKary() == KaryEnum.TEMPMUTE) {
            eb.addField("Kończy się o", sfd.format(new Date(kara.getEnd())), false);
            eb.addField("Nadano na czas", kara.getDuration(), false);
        }
        eb.addField("Typ & ID kary", KaryEnum.getName(kara.getTypKary()) +  " | " + kara.getKaraId(), false);

        if (bol || kara.getTypKary() == KaryEnum.UNBAN || kara.getTypKary() == KaryEnum.UNMUTE) {
            if (bol) {
                try {
                    BetterStringBuilder sb = new BetterStringBuilder();
                    sb.appendLine("Cześć!\n");
                    if (kara.getTypKary() == KaryEnum.UNBAN || kara.getTypKary() == KaryEnum.UNMUTE) {
                        String polski = kara.getTypKary() == KaryEnum.UNBAN ? "odbanowany" : "odciszony";
                        sb.appendLine("Właśnie zostałeś(-aś) **" + polski + "** z powodem **" + kara.getPowod() + "**");
                    } else {
                        sb.append("Właśnie dostałeś(-aś) karę **" + KaryEnum.getName(kara.getTypKary()) + "** z powodem **" +
                                kara.getPowod() + "**");
                        if (kara.getTypKary() == KaryEnum.TEMPBAN || kara.getTypKary() == KaryEnum.TEMPMUTE) {
                            sb.appendLine(" na czas **" + kara.getDuration() + "**");
                        }
                    }
                    sb.append("\n");
                    sb.appendLine("Czas nadania: " + sfd.format(new Date(kara.getTimestamp())));
                    sb.appendLine("ID kary: " + kara.getKaraId());
                    sb.append("--------------------------");
                    u.openPrivateChannel().complete().sendMessage(sb.build()).complete();
                    eb.addField("Udało się wysłać wiadomość o karze na pw?", "Tak", false);
                } catch (Exception e) {
                    eb.addField("Udało się wysłać wiadomość o karze na pw?", "Nie", false);
                }
            }
        } else {
            eb.addField("Aktywna jako pun?", kara.getPunAktywna() ? "Tak" : "Nie", false);
            if (kara.getMessageUrl() != null) {
                eb.addField("Kolega prosi o linka?", "[Klik](" + "https://discordapp.com/channels/" + kara.getMessageUrl() + ")", false);
            }
        }
        return eb;
    }

    public static BLanguage getLang() {
        BLanguage lang = new BLanguage();
        lang.setDay("d.");
        lang.setHour("gdz.");
        lang.setMinute("min.");
        lang.setSecond("sek.");
        return lang;
    }

}
