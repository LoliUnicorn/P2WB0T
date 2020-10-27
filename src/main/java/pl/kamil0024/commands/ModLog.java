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

package pl.kamil0024.commands;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
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
import pl.kamil0024.core.util.WebhookUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ModLog extends ListenerAdapter {

    private final ShardManager api;
    private final TextChannel modlog;
    private CaseDao caseDao;

    private ScheduledExecutorService executorSche;

    public ModLog(ShardManager api, CaseDao caseDao) {
        this.api = api;
        this.modlog = api.getTextChannelById(Ustawienia.instance.channel.modlog);
        if (modlog == null) {
            Log.newError("Kanał do modlogów jest nullem", ModLog.class);
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

        checkKara(event.getMember(), false, cc);
        //checkKara(event, true, caseDao.getNickAktywne(nick));
    }

    public synchronized void checkKara(Member event, boolean nick, List<CaseConfig> cc) {
        for (CaseConfig config : cc) {
            Kara k = config.getKara();
            String powod = null;

            String check = nick ? "Ten nick " : "Te konto ";

            switch (k.getTypKary()) {
                case BAN:
                    powod = check + "jest permanentnie zbanowane!";
                    event.getGuild().ban(event, 0, powod).queue();
                    break;
                case MUTE:
                    powod = check + "jest permanentnie wyciszone";
                    event.getGuild().addRoleToMember(event, Objects.requireNonNull(api.getRoleById(Ustawienia.instance.muteRole))).queue();
                    break;
                case TEMPBAN:
                    powod = check + "jest tymczasowo zbanowane";
                    TempbanCommand.tempban(event, api.retrieveUserById(config.getKara().getAdmId()).complete(), k.getPowod(), k.getDuration(), caseDao, this, true);
                    break;
                case TEMPMUTE:
                    powod = check + "jest tymczasowo wyciszone";
                    TempmuteCommand.tempmute(event, api.retrieveUserById(config.getKara().getAdmId()).complete(), k.getPowod(), k.getDuration(), caseDao, this, true);
                    break;
            }

            if (powod == null) return;

            Kara kara = new Kara();

            kara.setKaraId(caseDao.getAll().size() + 1);
            kara.setKaranyId(event.getId());
            kara.setMcNick(UserUtil.getMcNick(event));
            kara.setAdmId(k.getAdmId());
            kara.setPowod(powod + " (" + k.getPowod() + ") [ID: " + k.getKaraId() + "]");
            kara.setTimestamp(new Date().getTime());
            kara.setTypKary(k.getTypKary());
            kara.setAktywna(false);

            try {
                kara.setEnd(config.getKara().getEnd());
                kara.setDuration(config.getKara().getDuration());
            } catch (Exception ignored) { }

            if (!k.getPowod().contains("Te konto jest") && k.getKaraId() != 0) {
                if (nick) {
                    kara.setAktywna(true);
                    CaseConfig caseconfig = new CaseConfig(event.getId());
                    caseconfig.setKara(kara);
                    caseDao.save(caseconfig);

                    for (CaseConfig ccase : caseDao.getAktywe(event.getId())) {
                        caseDao.delete(ccase.getKara().getKaraId());
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
        List<User> bans = g.retrieveBanList().complete().stream().map(Guild.Ban::getUser).collect(Collectors.toList());

        for (CaseConfig a : cc) {
            Kara aCase = a.getKara();
            if (!aCase.getAktywna()) continue;

            KaryEnum typ = aCase.getTypKary();
            if (typ == KaryEnum.TEMPBAN || typ == KaryEnum.TEMPMUTE) {
                Long end = aCase.getEnd();
                if (end == null) {
                    Log.newError("Kara jest TEMPBAN || TEMPMUTE a getEnd() jest nullem ID:" + aCase.getKaraId(), ModLog.class);
                    continue;
                }
                if (end - data.getTime() <= 0) {
                    try {
                        User u = api.retrieveUserById(aCase.getKaranyId()).complete();
                        if (!bans.contains(u) && typ == KaryEnum.TEMPBAN) {
                            String msg = "Nie udało się dać kary UNBAN dla %s (ID: %s) bo: Typ nie ma bana";
                            Log.newError(String.format(msg, u.getId(), aCase.getKaraId()), ModLog.class);
                            continue;
                        }

                        if (u == null) continue;
                        Member m = null;
                        try {
                            m = g.retrieveMemberById(u.getId()).complete();
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
                        a.getKara().setAktywna(false);
                        caseDao.save(a);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "Nie udało się dać kary %s dla %s (ID: %s) bo: %s";
                        Log.newError(String.format(msg, typ == KaryEnum.TEMPBAN ? KaryEnum.UNBAN : KaryEnum.UNMUTE, aCase.getKaranyId(), aCase.getKaraId(), e.getMessage()), ModLog.class);
                    }
                    if (typ == KaryEnum.TEMPMUTE) {
                        User u = api.retrieveUserById(aCase.getKaranyId()).complete();
                        if (u != null) {
                            try {
                                Member m = g.retrieveMember(u).complete();
                                if (MuteCommand.hasMute(m)) {
                                    Log.newError("Uzytkownik " + UserUtil.getFullName(u) + " dostal unmuta, ale nadal ma range Wyciszony!", ModLog.class);
                                }
                            } catch (ErrorResponseException ignored) {}
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

    public static EmbedBuilder getEmbed(Kara kara, ShardManager api, Boolean bol) {
        return getEmbed(kara, api, bol, false);
    }

    @SuppressWarnings("ConstantConditions")
    public static EmbedBuilder getEmbed(Kara kara, ShardManager api, Boolean bol, Boolean seeAktywna) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy `@` HH:mm:ss");
        EmbedBuilder eb = new EmbedBuilder();
        User u = api.retrieveUserById(kara.getKaranyId()).complete();
        Member mem = null;
        User admUser = null;
        String adm = "Nie można było pobrać administratora. Jego ID to: " + kara.getAdmId();
        try {
            mem = api.getGuildById(Ustawienia.instance.bot.guildId).retrieveMemberById(kara.getAdmId()).complete();
        } catch (Exception ignored) {
            try {
                admUser = api.retrieveUserById(kara.getAdmId()).complete();
            } catch (Exception ignored1) { }
        }

        if (mem != null) eb.setColor(UserUtil.getColor(mem));
        eb.addField("Osoba karana", MarkdownSanitizer.escape(UserUtil.getFullName(u)), false);
        eb.addField("Nick w mc", MarkdownSanitizer.escape(kara.getMcNick()), false);
        eb.addField("Administrator",
                mem != null ? UserUtil.getFullName(mem.getUser()) : (admUser != null ? UserUtil.getLogName(admUser) : adm),
                false);
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
                eb.addField("Kolega prosi o linka?", "[Klik](" + "https://discord.com/channels/" + kara.getMessageUrl() + ")", false);
            }
            if (seeAktywna) eb.addField("Aktywna?", kara.getAktywna() ? "Tak" : "Nie", false);
            if (kara.getDowody() != null && kara.getDowody().size() != 0) {
                eb.addField("Dowody", "Do tej kary dołączono dowody. Aby je wyświetlić użyj komendy `dowod " + kara.getKaraId() + " list`", false);
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

    @Override
    public void onGuildBan(@Nonnull GuildBanEvent event) {
        sendCase(event.getGuild(), event.getUser(), ActionType.BAN);
    }

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        sendCase(event.getGuild(), event.getUser(), ActionType.UNBAN);
    }

    private void sendCase(Guild guild, User banowany, ActionType at) {
        String odpowiedzialny = "??";
        String powod = "??";
        try {
            List<AuditLogEntry> entries = guild.retrieveAuditLogs().type(at).complete();
            for (AuditLogEntry e : entries) {
                if (e.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(15)) &&
                        e.getTargetIdLong() == banowany.getIdLong()) {
                    if (e.getUser() == null) continue;
                    odpowiedzialny = UserUtil.getLogName(e.getUser());
                    powod = e.getReason();
                    break;
                }
            }
        } catch (Exception ignored) { }

        String action = at == ActionType.BAN ? "zbanował" : "odbanował";
        String format = "%s %s %s za `%s`";
        WebhookUtil web = new WebhookUtil();
        web.setType(WebhookUtil.LogType.CASES);
        web.setMessage(String.format(format, odpowiedzialny, action, UserUtil.getLogName(banowany), powod));
        web.send();
    }

}
