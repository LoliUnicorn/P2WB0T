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
