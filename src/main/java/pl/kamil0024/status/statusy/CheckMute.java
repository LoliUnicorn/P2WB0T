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

package pl.kamil0024.status.statusy;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.WebhookUtil;

import java.util.List;
import java.util.TimerTask;

public class CheckMute extends TimerTask {

    private final ShardManager api;

    public CheckMute(ShardManager api) {
        this.api = api;
    }

    @Override
    public void run() {
        Guild guild = api.getGuildById(Ustawienia.instance.bot.guildId);
        if (guild == null) {
            this.cancel();
            throw new NullPointerException("Serwer P2W jest nullem");
        }
//        Role muted = guild.getRoleById("515656024833785907");
        Role ekipa = guild.getRoleById("561102835715145728");
        Role ryba = guild.getRoleById("690267173872074832");
        for (Member member : guild.getMembers()) {
            if (!member.isOwner() && !member.getUser().isBot() && member.getOnlineStatus() != OnlineStatus.OFFLINE) {
                if (ryba == null) throw new NullPointerException("rola wyciszony jest nullem");
                assert !member.getRoles().contains(ekipa) && !member.getRoles().contains(ryba);
                try {
                    List<String> statusy = WulgarneStatusy.getAvtivity(member);
                    String inv = WulgarneStatusy.containsLink(statusy);
                    String wulgarne = WulgarneStatusy.containsSwear(statusy);
                    if ((inv != null && !inv.isEmpty()) || (wulgarne != null && !wulgarne.isEmpty())) {
                        String powod = inv == null ? wulgarne : inv;
                        if (powod.isEmpty()) throw new NullPointerException("powod jest nullem");
                        if (!member.getRoles().contains(ryba) && !member.getRoles().contains(ekipa)) {
                            guild.addRoleToMember(member, ryba).queue();
                            String txt = WulgarneStatusy.getPrivateChannel(member);
                            if (txt != null) {
                                TextChannel tc = guild.getTextChannelById(txt);
                                if (tc != null) {
                                    try {
                                        tc.putPermissionOverride(member).deny(Permission.VIEW_CHANNEL).queue();
                                    } catch (Exception e) {
                                        WebhookUtil wu = new WebhookUtil();
                                        wu.setType(WebhookUtil.LogType.ERROR);
                                        wu.setMessage(String.format("Chciano zabrać permy do kanału `%s`, ale wyskoczył błąd: ```\n%s\n```",
                                                tc.getName(), e.getLocalizedMessage()));
                                        wu.send();
                                    }
                                }
                            }
                            Thread t = new Thread(() -> {
                                WebhookUtil wu = new WebhookUtil();
                                wu.setType(WebhookUtil.LogType.STATUS);
                                wu.setMessage(String.format("Daje muta dla %s %s za `%s`",
                                        member.getAsMention(),
                                        UserUtil.getLogName(member), powod.replaceAll("@", "<małpa>")));
                                wu.send();
                            });
                            t.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}