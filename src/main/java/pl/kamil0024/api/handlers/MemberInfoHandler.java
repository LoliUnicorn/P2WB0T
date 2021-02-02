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

package pl.kamil0024.api.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class MemberInfoHandler implements HttpHandler {

    private final ShardManager api;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!Response.checkIp(ex)) { return; }

        try {
            MemberInfo memberInfo = new MemberInfo();
            Member mem = api.getGuildById(Ustawienia.instance.bot.guildId)
                    .getMemberById(ex.getQueryParameters().get("id").getFirst());

            if (mem != null) {
                if (mem.getNickname() != null) memberInfo.setNickname(mem.getNickname());
                memberInfo.setPermLevel(UserUtil.getPermLevel(mem));
                memberInfo.setRoles(mem.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
            } else memberInfo.setInGuild(false);

            Response.sendObjectResponse(ex, memberInfo);

        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Błąd!", "Nie udało się wykonać requesta!");
        }

    }

    @Data
    private static class MemberInfo {
        public MemberInfo() { }

        private String nickname;
        private boolean inGuild = true;
        private PermLevel permLevel;
        private List<String> roles = new ArrayList<>();

    }

}