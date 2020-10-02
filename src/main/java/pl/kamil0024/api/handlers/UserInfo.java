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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.util.UserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class UserInfo implements HttpHandler {

    private final ShardManager api;

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!CheckToken.checkToken(ex)) return;

        try {
            String id = ex.getQueryParameters().get("id").getFirst();
            if (id.isEmpty()) throw new Exception();

            User user = api.retrieveUserById(id).complete();
            Member mem = Objects.requireNonNull(api.getGuildById(Ustawienia.instance.bot.botId))
                    .retrieveMemberById(id).complete();
            if (user == null) {
                Response.sendErrorResponse(ex, "Błąd", "Taki użytkownik nie istnieje!");
                return;
            }
            FakeUser fake = new FakeUser();
            fake.setUsername(user.getName());
            fake.setAvatar(user.getAvatarUrl());
            if (mem != null) {
                String tak = UserUtil.getMcNick(mem);
                fake.setNick(tak.equals("-") ? "" : tak);
                List<FakeRole> roles = new ArrayList<>();
                for (Role role : mem.getRoles()) {
                    roles.add(new FakeRole(role.getName(), role.getColor() == null ? 0 : role.getColor().getRGB()));
                }
                fake.setRoles(roles);
            }
            Response.sendObjectResponse(ex, fake);
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd", "Nie udało się wysłać requesta! " + e.getMessage());
        }

    }

    @Data
    @AllArgsConstructor
    public static class FakeUser {
        public FakeUser() { }

        private String username;
        private String nick = null;
        private String avatar;
        private List<FakeRole> roles = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    private static class FakeRole {
        private String name;
        private int color = 0;
    }

}
