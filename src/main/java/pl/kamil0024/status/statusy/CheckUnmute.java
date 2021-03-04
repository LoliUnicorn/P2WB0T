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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.WebhookUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class CheckUnmute extends TimerTask {

    private final ShardManager api;

    public CheckUnmute(ShardManager api) {
        this.api = api;
    }

    @Override
    public void run() {
        Guild guild = api.getGuildById("422016694408577025");
        if (guild == null) {
            throw new NullPointerException("guild jest nullem");
        }
        TextChannel txt = guild.getTextChannelById("690269411755819148");
//        Role muted = guild.getRoleById("515656024833785907");
        Role ekipa = guild.getRoleById("561102835715145728");
        Role ryba = guild.getRoleById("690267173872074832");

        if (txt == null || ekipa == null || ryba == null) {
            throw new NullPointerException("chuj domyslaj sie co jest nullem");
        }

        for (Member member : txt.getMembers()) {
            if (member.getRoles().contains(ryba)) {
                List<String> statusy = new ArrayList<>(); // WulgarneStatusy.getAvtivity(member);
                String inv = ""; // WulgarneStatusy.containsLink(statusy);
                String wulgarne = ""; // WulgarneStatusy.containsSwear(statusy);
                if (inv == null && wulgarne == null) {
                    guild.removeRoleFromMember(member, ryba).queue();
                    String stringtxt = ""; // WulgarneStatusy.getPrivateChannel(member);
                    if (stringtxt != null) {
                        TextChannel tc = guild.getTextChannelById(stringtxt);
                        if (tc != null) {
                            try {
                                tc.putPermissionOverride(member).resetDeny().queue();
                            } catch (Exception e) {
                                WebhookUtil wu = new WebhookUtil();
                                wu.setType(WebhookUtil.LogType.ERROR);
                                wu.setMessage(String.format("Chciano oddać permy do kanału `%s`, ale wyskoczył błąd: ```\n%s\n```",
                                        tc.getName(), e.getLocalizedMessage()));
                                wu.send();
                            }
                        }
                    }
                    Thread t = new Thread(() -> {
                        WebhookUtil wu = new WebhookUtil();
                        wu.setType(WebhookUtil.LogType.STATUS);
                        wu.setMessage(String.format("Zdjemuje unmuta z użytkownika %s %s", member.getAsMention(), UserUtil.getLogName(member)));
                        wu.send();
                    });
                    t.start();
                }
            }
        }
    }
}
