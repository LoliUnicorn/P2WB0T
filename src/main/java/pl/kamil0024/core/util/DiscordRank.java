package pl.kamil0024.core.util;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pl.kamil0024.core.Ustawienia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum DiscordRank {

    GRACZ(Ustawienia.instance.rangi.gracz),
    VIP(Ustawienia.instance.rangi.vip),
    VIPPLUS(Ustawienia.instance.rangi.vipplus),
    MVP(Ustawienia.instance.rangi.mvp),
    MVPPLUS(Ustawienia.instance.rangi.mvpplus),
    MVPPLUSPLUS(Ustawienia.instance.rangi.mvpplusplus),
    SPONSOR(Ustawienia.instance.rangi.sponsor),
    MINIYT(Ustawienia.instance.rangi.miniyt),
    YT(Ustawienia.instance.rangi.yt),
    BUILDTEAM(Ustawienia.instance.rangi.buildteam),
    STAZYSTA(Ustawienia.instance.rangi.stazysta),
    POMOCNIK(Ustawienia.instance.rangi.pomocnik),
    MODERATOR(Ustawienia.instance.rangi.moderator),
    OWNER(Ustawienia.instance.rangi.korona),
    ADMINISTRATOR(Ustawienia.instance.rangi.administrator);

    private final String roleId;

    DiscordRank(String roleId) {
        this.roleId = roleId;
    }

    public static ArrayList<DiscordRank> getRanks(Member member) {
        ArrayList<DiscordRank> rank = new ArrayList<>();
        List<String> rolesId = member.getRoles().stream().map(Role::getId).collect(Collectors.toList());
        for (DiscordRank value : DiscordRank.values()) {
            if (rolesId.contains(value.getRoleId())) {
                rank.add(value);
            }
        }
        return rank;
    }

    public static ArrayList<DiscordRank> getRanks(String id, Guild guild) {
        return getRanks(guild.retrieveMemberById(id).complete());
    }

}
