package pl.kamil0024.stats.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.StatsDao;
import pl.kamil0024.core.database.config.StatsConfig;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.stats.entities.Statystyka;

import java.util.*;

public class TopCommand extends Command {

    private StatsDao statsDao;
    private EventWaiter eventWaiter;

    public TopCommand(StatsDao statsDao, EventWaiter eventWaiter) {
        name = "statsDao";
        permLevel = PermLevel.MODERATOR;
        this.statsDao = statsDao;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {

        if (UserUtil.getPermLevel(context.getMember()).getNumer() < PermLevel.ADMINISTRATOR.getNumer() && !context.getMember().getId().equals("416264257978761217")) {
            context.send("Nie możesz użyć tej komendy!").queue();
            return false;
        }

        Integer dni = context.getParsed().getNumber(context.getArgs().get(0));
        if (dni == null) throw new UsageException();

        List<StatsConfig> staty = statsDao.getAll();
        if (staty.isEmpty()) {
            context.send("Nikt nie ma statystyk lol").queue();
            return false;
        }

        HashMap<String, Suma> mapa = new HashMap<>();
        HashMap<String, Integer> top = new HashMap<>();

        for (StatsConfig statsConfig : staty) {
            int suma = 0;
            Statystyka statyZParuDni = StatsCommand.getStatsOfDayMinus(statsConfig.getStats(), dni);
            suma += (statyZParuDni.getNapisanychWiadomosci() +
                    statyZParuDni.getUsunietychWiadomosci() +

                    statyZParuDni.getWyrzuconych() +
                    statyZParuDni.getZbanowanych() +
                    statyZParuDni.getZmutowanych());

            mapa.put(statsConfig.getId(), new Suma(suma, statyZParuDni));
        }

        for (Map.Entry<String, Suma> entry : mapa.entrySet()) {
            top.put(entry.getKey(), entry.getValue().getNadaneKary());
        }
        top = sortByValue(top);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));

        return true;
    }

    @Data
    @AllArgsConstructor
    private class Suma {
        private Integer nadaneKary;
        private Statystyka statystyka;
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        List<Map.Entry<String, Integer> > list =
                new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
