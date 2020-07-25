package pl.kamil0024.api.handlers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.stats.commands.StatsCommand;
import pl.kamil0024.stats.entities.Statystyka;

import java.util.ArrayList;
import java.util.List;

public class StatsHandler implements HttpHandler {

    @Inject private APIModule api;
    @Inject private StatsDao statsDao;

    public StatsHandler(StatsDao statsDao, APIModule apiModule) {
        this.api = apiModule;
        this.statsDao = statsDao;
    }


    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
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
        List<StatsConfig> all = statsDao.getAll();

        for (StatsConfig statsc : all) {
            String mc = api.getUserConfig(statsc.getId()).getMcNick();
            if (mc == null) continue;
            if (mc.split(" ")[1].toLowerCase().equals(nick.toLowerCase())) {
                mem = statsc.getStats();
                break;
            }
        }

        if (mem.isEmpty()) {
            Response.sendErrorResponse(ex, "Pusta lista", "Ten gracz się leni i nic nie robi");
            return;
        }

        Statystyka stat = StatsCommand.getStatsOfDayMinus(mem, dni);
        Response.sendObjectResponse(ex, stat);

    }


}
