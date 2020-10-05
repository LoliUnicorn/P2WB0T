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
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.TicketDao;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.ticket.config.ChannelTicketConfig;
import pl.kamil0024.ticket.config.TicketRedisManager;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VoiceChatListener extends ListenerAdapter {

    private final TicketDao ticketDao;
    private final TicketRedisManager ticketRedisManager;
    private final EventWaiter eventWaiter;

    private final HashMap<String, Long> cooldown; // daj to później do redisa
    private final HashMap<String, String> messages;

    public VoiceChatListener(TicketDao ticketDao, TicketRedisManager ticketRedisManager, EventWaiter eventWaiter) {
        this.ticketDao = ticketDao;
        this.ticketRedisManager = ticketRedisManager;
        this.eventWaiter = eventWaiter;
        this.cooldown = new HashMap<>();
        this.messages = new HashMap<>();
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Guild guild = event.getGuild();
        if (event.getChannelJoined().getId().equals(Ustawienia.instance.ticket.vcToCreate)) {
            try {
                String[] name = event.getChannelLeft().getName().split(" ");
                VoiceChannel vc = guild.createVoiceChannel(name[name.length - 1].toLowerCase() + "-" + event.getMember().getId())
                        .setParent(guild.getCategoryById(Ustawienia.instance.ticket.createChannelCategory))
                        .addMemberPermissionOverride(event.getMember().getIdLong(),
                                Permission.getRaw(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK),
                                0)
                        .addRolePermissionOverride(guild.getPublicRole().getIdLong(), 0, Permission.getRaw(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK))
                        .complete();
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

                String msg = messages.get(event.getMember().getId());
                if (msg != null) {
                    TextChannel txt = event.getJDA().getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
                    try {
                        txt.retrieveMessageById(msg).complete().delete().complete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.newError("Nie udało się stworzyć kanału do ticketa!", VoiceChatListener.class);
                Log.newError(e, VoiceChatListener.class);
            }
            return;
        }
        checkRemoveTicket(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        String id = event.getChannelJoined().getId();
        if (event.getChannelJoined().getId().equals(id)) {
            ChannelTicketConfig ctc = ticketRedisManager.getChannel(event.getChannelJoined().getId());
            if (ctc != null && ctc.getAdmId() == null && UserUtil.getPermLevel(event.getMember()).getNumer() > 0) {
                ctc.setAdmId(event.getMember().getId());
                ticketRedisManager.removeChannel(id);
                ticketRedisManager.putChannelConfig(ctc);
            }
        }

        if (!event.getChannelJoined().getParent().getId().equals(Ustawienia.instance.ticket.strefaPomocy)) return;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            String memId = event.getMember().getId();
            GuildVoiceState state = event.getGuild().retrieveMemberById(memId).complete().getVoiceState();
            if (state != null && state.getChannel() != null && state.getChannel().getId().equals(id)) {
                long time = new Date().getTime();
                Long col = cooldown.get(memId);
                if (col == null || col - new Date().getTime() <= 0) {
                    TextChannel txt = event.getJDA().getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
                    if (txt == null) {
                        Log.newError("Kanał do powiadomień ticketów jest nullem!", VoiceChatListener.class);
                        return;
                    }
                    cooldown.put(memId, time + 30000);
                    String msg = "użytkownik <@%s> czeka na ";
                    String name = event.getChannelJoined().getName().toLowerCase();
                    if (name.contains("discord")) {
                        msg += "kanale pomocy serwera Discord!";
                    } else if (name.contains("p2w")) {
                        msg += "kanale pomocy forum P2W";
                    } else if (name.contains("minecraft")) {
                        msg += "kanale pomocy serwera Minecraft";
                    } else {
                        msg += "kanale pomocy, który nie jest wpisany do bota lol (" + name + ")";
                    }
                    Message mmsg = txt.sendMessage(msg).complete();
                    messages.put(memId, mmsg.getId());
                }
            }
        }, 30, 0, TimeUnit.SECONDS);

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

}
