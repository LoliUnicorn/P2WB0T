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

package pl.kamil0024.commands.moderation;

import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.UsageException;

public class KarainfoCommand extends Command {

    private final CaseDao caseDao;

    public KarainfoCommand(CaseDao caseDao) {
        name = "karainfo";
        aliases.add("infokara");
        permLevel = PermLevel.CHATMOD;
        category = CommandCategory.MODERATION;

        this.caseDao = caseDao;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        Integer id = context.getParsed().getNumber(context.getArgs().get(0));
        if (id == null) throw new UsageException();

        CaseConfig cc = caseDao.get(id);
        if (cc.getKara() == null) {
            context.send(context.getTranslate("karainfo.invalid")).queue();
            return false;
        }
        context.send(ModLog.getEmbed(cc.getKara(), context.getShardManager(), false, true).build()).queue();
        return true;
    }

}
