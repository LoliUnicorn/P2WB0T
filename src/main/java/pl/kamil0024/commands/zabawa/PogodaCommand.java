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

package pl.kamil0024.commands.zabawa;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.util.NetworkUtil;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.core.util.UserUtil;

public class PogodaCommand extends Command {

    public PogodaCommand() {
        name = "pogoda";
        aliases.add("weather");
        category = CommandCategory.ZABAWA;
        cooldown = 30;
        enabledInRekru = true;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        String lokacja = context.getArgsToString(0);
        if (context.getArgs().get(0) == null) throw new UsageException();

        try {
            String downloaded = new String(NetworkUtil.download("http://en.wttr.in/" +
                    NetworkUtil.encodeURIComponent(lokacja) + "?T"));
            downloaded = Jsoup.parse(downloaded).getElementsByTag("body").text();
            if (downloaded.startsWith("ERROR:")) {
                context.sendTranslate("pogoda.errorapi").queue();
                return false;
            }
            if (downloaded.contains("We were unable to find your location")) {
                context.sendTranslate("pogoda.badlocation").queue();
                return false;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(UserUtil.getColor(context.getMember()));
            eb.setTitle(context.getTranslate("pogoda.pogodaw", lokacja));
            eb.setImage("http://" + "pl.wttr.in/" +
                    NetworkUtil.encodeURIComponent(lokacja) + ".png?0m");
            context.send(eb.build()).queue();
        } catch (Exception e) {
            context.sendTranslate("pogoda.badlocation").queue();
            return false;
        }
        return true;
    }

}
