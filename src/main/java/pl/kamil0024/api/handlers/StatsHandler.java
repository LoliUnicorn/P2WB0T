package pl.kamil0024.api.handlers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.api.handlers.CheckToken;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.stats.commands.StatsCommand;
import pl.kamil0024.stats.entities.Statystyka;

import java.util.List;
import java.util.stream.Collectors;

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

        int dni = 0;
        String nick = ex.getQueryParameters().get("nick").getFirst();
        if (nick.isEmpty()) {
            Response.sendErrorResponse(ex, "Zły nick", "Nick jest pusty?");
            return;
        }

        try {
            dni = Integer.parseInt(ex.getQueryParameters().get("dni").getFirst());
            if (dni < 0) throw new Exception();
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Zła liczba dni", "Liczba dni jest nieprawidłowa lub jest mniejsza od zera");
            return;
        }

        Role r = api.getGuild().getRoleById(Ustawienia.instance.roles.chatMod);
        Member mem = null;
        try {
            mem = api.getGuild().getMembersWithRoles(r).stream()
                    .filter(m -> m.getNickname() != null && m.getNickname().split(" ")[1].toLowerCase().equals(nick.toLowerCase()))
                    .collect(Collectors.toList()).get(0);
        } catch (Exception ignored) {}

        if (mem == null) {
            Response.sendErrorResponse(ex, "Zły nick", "Ten nick nie istnieje, nie ma rangi ChatMod lub się leni i nic nie robi");
            return;
        }

        StatsConfig sc = statsDao.get(mem.getId());
        if (sc.getStats().isEmpty()) {
            Response.sendErrorResponse(ex, "Pusta lista", "Ten gracz się leni i nic nie robi");
            return;
        }

        Statystyka stat = StatsCommand.getStatsOfDayMinus(sc.getStats(), dni);
        Response.sendObjectResponse(ex, stat);

    }

}
