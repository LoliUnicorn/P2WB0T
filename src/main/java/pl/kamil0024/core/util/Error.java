package pl.kamil0024.core.util;

import net.dv8tion.jda.api.EmbedBuilder;
import pl.kamil0024.commands.system.HelpCommand;
import pl.kamil0024.core.command.CommandContext;

public class Error {

    public static void usageError(CommandContext context) {
        EmbedBuilder eb = HelpCommand.getUsage(context);
        context.getEvent().getTextChannel().sendMessage(eb.build()).queue();
    }

    public static EmbedBuilder getUsageEmbed(CommandContext context) {
        return HelpCommand.getUsage(context);
    }

}
