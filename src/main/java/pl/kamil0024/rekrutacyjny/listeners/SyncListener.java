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

package pl.kamil0024.rekrutacyjny.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.DiscordRank;
import pl.kamil0024.core.util.UserUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("ConstantConditions")
public class SyncListener extends ListenerAdapter {
    
    public SyncListener() { }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.rekrutacyjny.guildId)) return;
        updateMember(event.getMember(), getDerpMember(event.getMember()));
    } 
    
    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;
        updateMember(getRekruMember(event.getMember()), event.getMember());
    }
    
    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;
        updateMember(getRekruMember(event.getMember()), event.getMember());
    }

    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;
        updateMember(getRekruMember(event.getMember()), event.getMember());
    }
    
    public static void updateMember(@Nullable Member mem, @Nullable Member derp) {
        if (mem == null || derp == null || UserUtil.getPermLevel(derp).getNumer() == PermLevel.MEMBER.getNumer()) return;

        for (DiscordRank rank : UserUtil.getRanks(mem)) {
            String prefix = null;
            String role = null;
            switch (rank) {
                case POMOCNIK:
                    role = Ustawienia.instance.rekrutacyjny.pom;
                    prefix = "POM";
                    break;
                case STAZYSTA:
                    role = Ustawienia.instance.rekrutacyjny.staz;
                    prefix = "REKRUT";
                    break;
                case MODERATOR:
                    role = Ustawienia.instance.rekrutacyjny.mod;
                    prefix = "MOD";
                    break;
                case ADMINISTRATOR:
                    role = Ustawienia.instance.rekrutacyjny.admin;
                    prefix = "ADM";
                    break;
                case CHATMOD:
                    role = Ustawienia.instance.rekrutacyjny.chatmod;
                    prefix = "CHATMOD";
                    break;
            }
            if (role == null) return;
            try {
                mem.getGuild().modifyNickname(mem, "[" + prefix + "] " + UserUtil.getMcNick(derp)).complete();
                mem.getGuild().addRoleToMember(mem, Objects.requireNonNull(mem.getGuild().getRoleById(role))).complete();
            } catch (Exception e) {
                Log.newError(e, SyncListener.class);
            }
        }
        
    }

    @Nullable
    private Member getDerpMember(Member mem) {
        try {
            return mem.getJDA().getGuildById(Ustawienia.instance.bot.guildId).retrieveMemberById(mem.getId()).complete();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private Member getRekruMember(Member mem) {
        try {
            return mem.getJDA().getGuildById(Ustawienia.instance.rekrutacyjny.guildId).retrieveMemberById(mem.getId()).complete();
        } catch (Exception e) {
            return null;
        }
    }

}
