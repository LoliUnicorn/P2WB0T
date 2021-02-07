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

import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.kolkoikrzyzyk.KolkoIKrzyzykManager;
import pl.kamil0024.commands.kolkoikrzyzyk.entites.Zaproszenie;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.util.UsageException;

public class KolkoIKrzyzykCommand extends Command {

    private final KolkoIKrzyzykManager kolkoIKrzyzykManager;

    public KolkoIKrzyzykCommand(KolkoIKrzyzykManager kolkoIKrzyzykManager) {
        name = "kolkoikrzyzyk";
        aliases.add("kolko");
        aliases.add("krzyzyk");
        cooldown = 15;
        category = CommandCategory.ZABAWA;

        this.kolkoIKrzyzykManager = kolkoIKrzyzykManager;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();

        if (arg.equalsIgnoreCase("akceptuj")) {
            Integer id = context.getParsed().getNumber(context.getArgs().get(1));
            if (id == null) {
                context.sendTranslate("kolkoikrzyzyk.emptyid").queue();
                return false;
            }

            Zaproszenie zapro = kolkoIKrzyzykManager.getZaproById(id);
            if (zapro == null || !zapro.getZapraszajaGo().equals(context.getUser().getId())) {
                context.sendTranslate("kolkoikrzyzyk.badid").queue();
                return false;
            }
            kolkoIKrzyzykManager.nowaGra(zapro);
            return true;
        } else {
            Member member = context.getParsed().getMember(context.getArgs().get(0));
            if (member == null) {
                context.sendTranslate("kolkoikrzyzyk.badmember").queue();
                return false;
            }
            if (member.getId().equals(context.getUser().getId())) {
                context.sendTranslate("kolkoikrzyzyk.nofriend").queue();
                return false;
            }

            if (member.getUser().isBot()) {
                context.sendTranslate("kolkoikrzyzyk.bot").queue();
                return false;
            }

            KolkoIKrzyzykManager.ZaproszenieStatus zapro = kolkoIKrzyzykManager.zapros(context.getMember(), member, context.getChannel());
            if (!zapro.isError()) {
                Zaproszenie zapka = kolkoIKrzyzykManager.getZaproszenia().get(context.getUser().getId());
                if (zapka == null) throw new NullPointerException("zapka == null");
                context.send(String.format(zapro.getMsg(), zapka.getId())).queue();
            } else context.send(zapro.getMsg());

            return !zapro.isError();
        }
    }

}
