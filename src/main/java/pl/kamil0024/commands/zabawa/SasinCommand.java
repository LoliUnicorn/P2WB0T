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

import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.util.UsageException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SasinCommand extends Command {

    public SasinCommand() {
        name = "sasin";
        category = CommandCategory.ZABAWA;
        enabledInRekru = true;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        if (context.getArgs().get(0) == null) throw new UsageException();

        try {
            int liczba = Integer.parseInt(context.getArgs().get(0));
            BigDecimal sasiny = new BigDecimal(liczba / 70_000_000d).setScale(9, RoundingMode.HALF_UP);
            String sasinyStr;
            if (sasiny.intValue() == sasiny.doubleValue()) sasinyStr = String.valueOf(sasiny.intValue());
            else sasinyStr = sasiny.toPlainString();
            context.sendTranslate("sasin.result", liczba, sasinyStr).reference(context.getMessage()).queue();
            return true;
        } catch (Exception e) {
            context.send("Zła liczba!").queue();
            return false;
        }
    }

}
