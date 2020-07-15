package pl.kamil0024.core.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;

import java.awt.*;
import java.util.List;

public class UserUtil {

    public static boolean isOwner(User user) {
        List<String> xd = Ustawienia.instance.devs;
        for (String id : xd) { if (user.getId().equals(id)) return true; }
        return false;
    }

    public static PermLevel getPermLevel(User user) {
        Guild g = user.getJDA().getGuildById(Ustawienia.instance.bot.guildId);
        if (g == null) throw new IllegalArgumentException("Ustawienia.instance.bot.guildId == null");
        try {
            Member mem = g.retrieveMemberById(user.getId()).complete();
            return getPermLevel(mem);
        } catch (Exception e) {
            return PermLevel.MEMBER;
        }
    }

    public static PermLevel getPermLevel(@Nullable Member member) {
        if (member == null) return PermLevel.MEMBER;
        if (isOwner(member.getUser())) return PermLevel.DEVELOPER;
        if (member.getId().equals(Ustawienia.instance.bot.botId)) return PermLevel.ADMINISTRATOR;
        PermLevel lvl = PermLevel.MEMBER;
        for (Role rol : member.getRoles()) {
            if (rol.getId().equals(Ustawienia.instance.roles.adminRole)) return PermLevel.ADMINISTRATOR;

            if (rol.getId().equals(Ustawienia.instance.roles.moderatorRole)) {
                if (PermLevel.MODERATOR.getNumer() > lvl.getNumer()) lvl = PermLevel.MODERATOR;
            }
            if (rol.getId().equals(Ustawienia.instance.roles.helperRole) || rol.getId().equals(Ustawienia.instance.roles.chatMod)) {
                if (PermLevel.HELPER.getNumer() > lvl.getNumer()) lvl = PermLevel.HELPER;
            }
        }
        return lvl;
    }

    public static Color getColor(Member member) {
        for (Role r : member.getRoles()) {
            if (r.getColor() != null) return r.getColor();
        }
        return Color.GREEN;
    }

    public static String getName(JDA jda, String id) {
        User u = jda.retrieveUserById(id).complete();
        if (u == null) return null;
        return getName(u);
    }

    public static String getName(User u) {
        return u.getName() + "#" + u.getDiscriminator();
    }

    public static String getFullName(JDA jda, String id) {
        User u = jda.retrieveUserById(id).complete();
        if (u == null) return null;
        return getFullName(u);
    }

    public static String getFullName(User u) {
        return u.getAsMention() + " [" + getName(u) + "]";
    }

    public static String getFullNameMc(Member mem) {
        return mem.getAsMention() + " [" + getName(mem.getUser()) + "]" + "(`" + getMcNick(mem) + "`)";
    }

    public static String getLogName(User u) {
        return u.getAsMention() + " " + getName(u) + "[" + u.getId() + "]";
    }

    public static String getLogName(Member member) {
        return getLogName(member.getUser());
    }

    public static String getMcNick(@Nullable Member member, boolean seeNick) {
        if (member == null) return "-";
        String nick = member.getNickname();
        if (nick == null) {
            if (seeNick) return getName(member.getUser());
            else return "-";
        }
        if (nick.contains("[")) nick = nick.split(" ")[1];
        return nick;
    }

    public static String getMcNick(@Nullable Member member) {
        return getMcNick(member, false);
    }

}
