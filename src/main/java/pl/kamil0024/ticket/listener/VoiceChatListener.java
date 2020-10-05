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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.database.TicketDao;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.ticket.config.ChannelTicketConfig;
import pl.kamil0024.ticket.config.TicketRedisManager;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VoiceChatListener extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(VoiceChatListener.class);
    private final TicketDao ticketDao;
    private final TicketRedisManager ticketRedisManager;
    private final EventWaiter eventWaiter;

    public VoiceChatListener(TicketDao ticketDao, TicketRedisManager ticketRedisManager, EventWaiter eventWaiter) {
        this.ticketDao = ticketDao;
        this.ticketRedisManager = ticketRedisManager;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        logger.debug("Event wykonany\n" + event.getChannelJoined().getId());
        Guild guild = event.getGuild();
        if (event.getChannelJoined().getId().equals(Ustawienia.instance.ticket.vcToCreate)) {
            logger.debug("tworzę kanał...");
            try {
                VoiceChannel vc = guild.createVoiceChannel("ticket")
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
                            && vc.getId().equals(auditlog.getOption(AuditLogOption.CHANNEL)) && !auditlog.getUser().isBot()) {
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
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        TextChannel txt = event.getJDA().getTextChannelById(Ustawienia.instance.ticket.notificationChannel);
        if (txt == null) {
            Log.newError("Kanał do powiadomień ticketów jest nullem!", VoiceChatListener.class);
            return;
        }
        String id = event.getChannel().getId();
        ChannelTicketConfig conf = ticketRedisManager.getChannel(id);
        ticketRedisManager.removeChannel(id);
        if (conf != null) {
            String st = "<@%s>, czy chcesz wysłać ankietę dotyczącą tego zgłoszenia do gracza <@%s>?";
            Message msg = txt.sendMessage(String.format(st, conf.getAdmId(), conf.getUserId())).complete();
            Emote red = event.getJDA().getEmoteById(Ustawienia.instance.emote.red);
            Emote green = event.getJDA().getEmoteById(Ustawienia.instance.emote.green);
            msg.addReaction(Objects.requireNonNull(green)).queue();
            msg.addReaction(Objects.requireNonNull(red)).queue();
            eventWaiter.waitForEvent(MessageReactionAddEvent.class,
                    (e) -> e.getUser().getId().equals(conf.getAdmId()),
                    (e) -> {
                        if (e.getReactionEmote().getId().equals(red.getId())) {
                            msg.delete().queue();
                            return;
                        }
                        ticketDao.sendMessage(event.getGuild().retrieveMemberById(conf.getUserId()).complete(), conf.getAdmId());
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
