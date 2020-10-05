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

package pl.kamil0024.ticket.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.joda.time.DateTime;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.TicketDao;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.redis.Cache;
import pl.kamil0024.core.redis.RedisManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.ticket.config.ChannelTicketConfig;
import pl.kamil0024.ticket.config.TicketRedisManager;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VoiceChatListener extends ListenerAdapter {

    private final TicketDao ticketDao;
    private final TicketRedisManager ticketRedisManager;
    private final EventWaiter eventWaiter;
    private final ShardManager api;

    private final HashMap<String, Long> cooldown; // daj to później do redisa
    private final HashMap<String, String> messages;

    private HashMap<String, List<String>> adms;

    public VoiceChatListener(TicketDao ticketDao, TicketRedisManager ticketRedisManager, EventWaiter eventWaiter, ShardManager api) {
        this.api = api;
        this.ticketDao = ticketDao;
        this.ticketRedisManager = ticketRedisManager;
        this.eventWaiter = eventWaiter;
        this.cooldown = new HashMap<>();
        this.messages = new HashMap<>();
        this.adms = new HashMap<>();

        Role chatMod = api.getRoleById(Ustawienia.instance.roles.chatMod);
        Role ekipa = api.getRoleById("561102835715145728");
        Objects.requireNonNull(api.getGuildById(Ustawienia.instance.bot.guildId))
                .loadMembers().onSuccess(m -> m.stream()
                    .filter(mem -> mem.getRoles().contains(chatMod) || mem.getRoles().contains(ekipa))
                    .collect(Collectors.toList()).forEach(member -> adms.put(member.getId(), member.getRoles().stream().map(Role::getId).collect(Collectors.toList()))));
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Guild guild = event.getGuild();
        if (event.getChannelJoined().getId().equals(Ustawienia.instance.ticket.vcToCreate)) {
            try {
                String[] name = event.getChannelLeft().getName().split(" ");
                Category cate = Objects.requireNonNull(guild.getCategoryById(Ustawienia.instance.ticket.createChannelCategory));

                ChannelAction<VoiceChannel> action = guild.createVoiceChannel(name[name.length - 1].toLowerCase() + "-" + event.getMember().getId())
                        .setParent(cate)
                        .addMemberPermissionOverride(event.getMember().getIdLong(),
                                Permission.getRaw(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VIEW_CHANNEL),
                                0)
                        .addRolePermissionOverride(561102835715145728L,
                                Permission.getRaw(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VIEW_CHANNEL)
                                , 0)
                        .addRolePermissionOverride(guild.getPublicRole().getIdLong(), 0, Permission.getRaw(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VIEW_CHANNEL));

                for (PermissionOverride permissionOverride : Objects.requireNonNull(cate).getPermissionOverrides()) {
                    if (permissionOverride.getPermissionHolder() != null) {
                        action = action.addPermissionOverride(permissionOverride.getPermissionHolder(), permissionOverride.getAllowed(), permissionOverride.getDenied());
                    }
                }
                VoiceChannel vc = action.complete();
                guild.moveVoiceMember(event.getMember(), vc).queue();

                List<AuditLogEntry> audit = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_VOICE_MOVE).complete();
                User adm = null;
                for (AuditLogEntry auditlog : audit) {
                    if (auditlog.getType() == ActionType.MEMBER_VOICE_MOVE
                            && auditlog.getTimeCreated().isAfter(OffsetDateTime.now().minusMinutes(1))
                            && Ustawienia.instance.ticket.vcToCreate.equals(auditlog.getOption(AuditLogOption.CHANNEL)) && !auditlog.getUser().isBot()) {
                        adm = auditlog.getUser();
                        break;
                    }
                }

                ChannelTicketConfig ctc = new ChannelTicketConfig();
                if (adm != null) ctc.setAdmId(adm.getId());
                ctc.setChannelId(vc.getId());
                ctc.setCreatedTime(new Date().getTime());
                ctc.setUserId(event.getMember().getId());
                ticketRedisManager.putChannelConfig(ctc);

                deleteMessage(event.getMember().getId(), event.getJDA());
            } catch (Exception e) {
                e.printStackTrace();
                Log.newError("Nie udało się stworzyć kanału do ticketa!", VoiceChatListener.class);
                Log.newError(e, VoiceChatListener.class);
            }
            return;
        }
        checkRemoveTicket(event.getChannelLeft());
        channelJoin(event.getMember(), event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        channelJoin(event.getMember(), event.getChannelJoined());
    }

    private void channelJoin(Member mem, VoiceChannel channelJoined) {
        String id = channelJoined.getId();
        String memId = mem.getId();
        ChannelTicketConfig ctc = ticketRedisManager.getChannel(channelJoined.getId());
        if (ctc != null) {
            if (ctc.getAdmId() == null && UserUtil.getPermLevel(mem).getNumer() > 0) {
                ctc.setAdmId(mem.getId());
                ticketRedisManager.removeChannel(id);
                ticketRedisManager.putChannelConfig(ctc);
                return;
            }
        }
        if (!channelJoined.getParent().getId().equals(Ustawienia.instance.ticket.strefaPomocy)) return;

        Runnable task = () -> {
            GuildVoiceState state = channelJoined.getGuild().retrieveMemberById(memId).complete().getVoiceState();
            if (state != null && state.getChannel() != null && state.getChannel().getId().equals(id)) {
                Long col = cooldown.get(memId);
                if (col == null || col - new Date().getTime() <= 0) {
                    TextChannel txt = channelJoined.getGuild().getJDA().getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
                    if (txt == null) {
                        Log.newError("Kanał do powiadomień ticketów jest nullem!", VoiceChatListener.class);
                        return;
                    }
                    cooldown.put(memId, new DateTime().plusMinutes(10).getMillis());
                    String msg = String.format("użytkownik <@%s> czeka na ", memId);
                    String name = channelJoined.getName().toLowerCase();
                    if (name.contains("discord")) {
                        msg += "kanale pomocy serwera Discord!\n\n" + getMentions(channelJoined.getGuild(), Ustawienia.instance.roles.chatMod);
                    } else if (name.contains("p2w")) {
                        msg += "kanale pomocy forum P2W\n\n" + getMentions(channelJoined.getGuild(), "561102835715145728");
                    } else if (name.contains("minecraft")) {
                        msg += "kanale pomocy serwera Minecraft\n\n" + getMentions(channelJoined.getGuild(), "561102835715145728");
                    } else {
                        msg += "kanale pomocy, który nie jest wpisany do bota lol (" + name + ")";
                    }
                    Message mmsg = txt.sendMessage(msg).complete();
                    deleteMessage(memId, channelJoined.getJDA());
                    messages.put(memId, mmsg.getId());
                }
            }
        };
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.schedule(task, 30, TimeUnit.SECONDS);
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        TextChannel txt = event.getJDA().getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
        if (txt == null) {
            Log.newError("Kanał do powiadomień ticketów jest nullem!", VoiceChatListener.class);
            return;
        }

        String id = event.getChannel().getId();
        ChannelTicketConfig conf = ticketRedisManager.getChannel(id);
        if (conf != null) {
            String st = "<@%s>, czy chcesz wysłać ankietę dotyczącą tego zgłoszenia do gracza <@%s>?";
            Message msg = txt.sendMessage(String.format(st, conf.getAdmId(), conf.getUserId()))
                    .allowedMentions(Collections.singleton(Message.MentionType.USER))
                    .complete();
            Emote red = event.getJDA().getEmoteById(Ustawienia.instance.emote.red);
            Emote green = event.getJDA().getEmoteById(Ustawienia.instance.emote.green);
            msg.addReaction(Objects.requireNonNull(green)).queue();
            msg.addReaction(Objects.requireNonNull(red)).queue();
            ticketRedisManager.removeChannel(id);
            eventWaiter.waitForEvent(MessageReactionAddEvent.class,
                    (e) -> e.getUser().getId().equals(conf.getAdmId()),
                    (e) -> {
                        if (e.getReactionEmote().getId().equals(red.getId())) {
                            msg.delete().queue();
                            return;
                        }
                        ticketDao.sendMessage(event.getGuild().retrieveMemberById(conf.getUserId()).complete(), conf.getAdmId(), conf);
                        msg.delete().queue();
                    },
                    30, TimeUnit.SECONDS,
                    () -> msg.delete().queue());
        }

    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        checkRemoveTicket(event.getChannelLeft());
        deleteMessage(event.getMember().getId(), event.getJDA());
    }

    private void checkRemoveTicket(VoiceChannel voiceChannel) {
        if (voiceChannel.getMembers().size() == 0
                && voiceChannel.getParent().getId().equals(Ustawienia.instance.ticket.createChannelCategory)
                && !voiceChannel.getId().equals(Ustawienia.instance.ticket.vcToCreate)) {
            try {
                voiceChannel.delete().complete();
            } catch (Exception e) {
                Log.newError("Nie udało się usunąć kanału z ticketem!", VoiceChatListener.class);
                Log.newError(e, VoiceChatListener.class);
            }
        }
    }

    public void deleteMessage(String id, JDA jda) {
        TextChannel xd = jda.getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
        try {
            String msg = messages.get(id);
            if (msg == null) return;
            xd.retrieveMessageById(msg).complete().delete().complete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMentions(Guild g, String... roleId) {
        List<String> users = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : adms.entrySet()) {
            for (String s : roleId) {
                if (entry.getValue().contains(s)) {
                    OnlineStatus mem = g.retrieveMemberById(entry.getKey()).complete().getOnlineStatus();
                    if (mem == OnlineStatus.IDLE || mem == OnlineStatus.INVISIBLE || mem == OnlineStatus.OFFLINE) {
                        continue;
                    }
                    users.add(entry.getKey());
                }
            }
        }
        users.remove("322644408799461377");
        users.remove("760876062606360627");
        StringBuilder mentionS = new StringBuilder();
        users.forEach(us -> mentionS.append(String.format("<@%s> ", us)));
        return mentionS.toString();
    }

}
