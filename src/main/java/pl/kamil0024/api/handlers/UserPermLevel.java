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
import lombok.Data;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UserUtil;

public class UserPermLevel implements HttpHandler {

    private final ShardManager api;

    public UserPermLevel(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!CheckToken.checkToken(ex)) return;
        String user = ex.getRequestHeaders().get("User").getFirst();
        if (user == null || user.isEmpty()) {
            Response.sendErrorResponse(ex, "Error", "Header `user` jest nullem");
            return;
        }
        try {
            User u = api.retrieveUserById(user).complete();
            if (u == null) throw new Exception();
            PermLevel pl = UserUtil.getPermLevel(u);
            Response.sendObjectResponse(ex, new UserPermLevelClass(pl));
        } catch (Exception exception) {
            Response.sendErrorResponse(ex, "Error", "Złe id?");
        }
    }

    @Data
    public static class UserPermLevelClass {
        private final String name;
        private final int numer;

        public UserPermLevelClass(PermLevel permLevel) {
            this.name = permLevel.toString();
            this.numer = permLevel.getNumer();
        }
    }

}
