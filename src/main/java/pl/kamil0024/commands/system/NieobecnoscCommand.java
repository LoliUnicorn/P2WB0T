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

package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.NieobecnosciDao;
import pl.kamil0024.core.database.config.NieobecnosciConfig;
import pl.kamil0024.core.util.EmbedPageintaor;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.nieobecnosci.NieobecnosciManager;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;
import pl.kamil0024.nieobecnosci.config.Zmiana;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NieobecnoscCommand extends Command {

    private final NieobecnosciManager nieobecnosciManager;
    private final EventWaiter eventWaiter;
    private final NieobecnosciDao nieobecnosciDao;

    public NieobecnoscCommand(NieobecnosciManager nieobecnosciManager, EventWaiter eventWaiter, NieobecnosciDao nieobecnosciDao) {
        name = "nieobecnosc";
        aliases.add("nieobecnosci");
        aliases.add("nb");
        permLevel = PermLevel.ADMINISTRATOR;
        this.nieobecnosciManager = nieobecnosciManager;
        this.eventWaiter = eventWaiter;
        this.nieobecnosciDao = nieobecnosciDao;
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();

        if (arg.equalsIgnoreCase("aktywne")) {
            ArrayList<Nieobecnosc> nball = nieobecnosciDao.getAllAktywne();
            if (nball.isEmpty()) {
                context.send("Nie ma aktywnych nieobecności!").queue();
                return false;
            }
            List<EmbedBuilder> pages = new ArrayList<>();
            for (Nieobecnosc nieobecnosc : nball) {
                Member mem = context.getParsed().getMember(nieobecnosc.getUserId());
                if (mem == null) continue;
                EmbedBuilder eb = NieobecnosciManager.getEmbed(nieobecnosc, mem);
                List<Zmiana> zmiany = nieobecnosc.getZmiany();
                eb.addField("Ilość zmian:", zmiany == null ? "0" : zmiany + "", false);
                eb.addField("Ostatnia zmiana:",
                        zmiany == null || zmiany.isEmpty() ? "Brak." : zmiany.get(zmiany.size() - 1).toString(context.getGuild())
                        , false);
                pages.add(eb);
            }
            new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA(), 320)
                    .create(context.getChannel());
            return true;
        }
        Member mem = context.getParsed().getMember(arg);
        if (mem != null) {
            NieobecnosciConfig nbConf = nieobecnosciDao.get(mem.getId());
            if (nbConf.getNieobecnosc().isEmpty()) {
                context.send("Ten użytkownik nie miał jeszcze nieobecnośći!").queue();
                return false;
            }
            List<EmbedBuilder> pages = new ArrayList<>();
            for (Nieobecnosc nieobecnosc : nbConf.getNieobecnosc()) {
                pages.add(NieobecnosciManager.getEmbed(nieobecnosc, mem));
            }
            return true;
        }
        if (arg.equalsIgnoreCase("powod")) {
            String rawMember = context.getArgs().get(1);
            String powod = context.getArgsToString(2);
            if (rawMember == null || powod == null || powod.isEmpty()) {
                context.send("Użycie: nieobecnosc powod <member> <nowy powód>").queue();
                return false;
            }
            Member maNieobecnosc = context.getParsed().getMember(arg);
            if (maNieobecnosc == null) {
                context.send("Użycie: nieobecnosc powod <member> <nowy powód>").queue();
                return false;
            }
            NieobecnosciConfig nbConf = nieobecnosciDao.get(maNieobecnosc.getId());
            if (nbConf.getNieobecnosc().isEmpty()) {
                context.send("Ten użytkownik nie ma żadnej nieobecności!").queue();
                return false;
            }
            Nieobecnosc last = nbConf.getNieobecnosc().get(nbConf.getNieobecnosc().size() - 1);
            nbConf.getNieobecnosc().remove(last);
            Zmiana zmiana = new Zmiana();
            zmiana.setKtoZmienia(context.getUser().getId());
            zmiana.setCoZmienia(Zmiana.Enum.REASON);
            zmiana.setKiedy(new Date().getTime());
            zmiana.setKomentarz("Stary powód: " + last.getPowod() + "\nNowy powód:" + powod);
            zmiana.sendLog(context.getGuild(), last.getUserId(), last.getId());
            if (last.getZmiany() == null) last.setZmiany(new ArrayList<>());
            last.getZmiany().add(zmiana);
            nbConf.getNieobecnosc().add(last);
            nieobecnosciDao.save(nbConf);
            context.send("Pomyślnie zmieniono powód!").queue();
            nieobecnosciManager.update();
            return true;
        }

        throw new UsageException();
    }

}
