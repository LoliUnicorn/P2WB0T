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

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.stats.commands.StatsCommand;
import pl.kamil0024.stats.entities.Statystyka;

import java.util.ArrayList;
import java.util.List;

public class StatsHandler implements HttpHandler {

    @Inject private final APIModule api;
    @Inject private final StatsDao statsDao;
    @Inject private boolean id = false;

    public StatsHandler(StatsDao statsDao, APIModule apiModule) {
        this.api = apiModule;
        this.statsDao = statsDao;
    }

    public StatsHandler(StatsDao statsDao, APIModule apiModule, boolean id) {
        this.api = apiModule;
        this.statsDao = statsDao;
        this.id = id;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!CheckToken.checkToken(ex)) return;

        int dni;
        String nick = ex.getQueryParameters().get("nick").getFirst();
        if (nick.isEmpty()) {
            Response.sendErrorResponse(ex, "Zły nick", "Nick jest pusty?");
            return;
        }

        try {
            dni = Integer.parseInt(ex.getQueryParameters().get("dni").getFirst());
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Zła liczba dni", "Liczba dni jest nieprawidłowa lub jest mniejsza od zera");
            return;
        }

        if (dni < 0) {
            Response.sendErrorResponse(ex, "Zła liczba dni", "Liczba dni jest nieprawidłowa lub jest mniejsza od zera");
            return;
        }

        ArrayList<Statystyka> mem = new ArrayList<>();

        if (!id) {
            List<StatsConfig> all = statsDao.getAll();
            for (StatsConfig statsc : all) {
                String mc = api.getUserConfig(statsc.getId()).getMcNick();
                if (mc == null) continue;
                if (mc.split(" ")[1].equalsIgnoreCase(nick)) {
                    mem = statsc.getStats();
                    break;
                }
            }
        } else {
            mem = statsDao.get(nick).getStats();
        }

        if (mem.isEmpty()) {
            Response.sendErrorResponse(ex, "Pusta lista", "Ten gracz się leni i nic nie robi");
            return;
        }

        Statystyka stat = StatsCommand.getStatsOfDayMinus(mem, dni);
        Response.sendObjectResponse(ex, stat);

    }


}
