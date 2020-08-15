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

package pl.kamil0024.core.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.command.CommandContext;

import java.util.List;
import java.util.stream.Collectors;

public class TextChannelArgument extends Args {

    public TextChannelArgument() {
        name = "textchannel";
    }

    @Override
    public TextChannel parsed(String s, @Nullable JDA jda, CommandContext context) {
        List<TextChannel> tchannel = context.getGuild().getTextChannels().stream()
                .filter(channel -> s.equals(channel.getName()) || s.equals(channel.getId()) ||
                        s.equals(channel.getAsMention())).collect(Collectors.toList());
        if (tchannel.size() != 1) return null;
        return tchannel.get(0);
    }

}
