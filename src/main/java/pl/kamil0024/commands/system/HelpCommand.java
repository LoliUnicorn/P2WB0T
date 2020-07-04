package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.util.UserUtil;

import java.util.Arrays;
import java.util.Map;

public class HelpCommand extends Command {

    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        name = "help";
        aliases = Arrays.asList("komendybota", "pomoc");
        this.commandManager = commandManager;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgs().get(0);

        if (arg == null || arg.isEmpty()) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(context.getTranslate("help.title", commandManager.getCommands().size()));
            eb.setFooter(context.getTranslate("help.footer"));
            eb.setColor(UserUtil.getColor(context.getMember()));
            eb.setDescription(context.getTranslate("help.desc", context.getPrefix()));

            for (CommandCategory cate : CommandCategory.values()) {
                StringBuffer komendy = new StringBuffer();
                for (Map.Entry<String, Command> cmd : commandManager.getCommands().entrySet()) {
                    if (cmd.getValue().getPermLevel().getNumer() <= UserUtil.getPermLevel(context.getMember()).getNumer()) {
                        if (cmd.getValue().getCategory() == cate) { komendy.append(cmd.getKey()).append("`,` "); }
                    }
                }
                if (!komendy.toString().isEmpty()) eb.addField(context.getTranslate("category." + cate.toString().toLowerCase()), komendy.toString(), false);
            }

            context.send(eb.build()).queue();
            return true;
        }

        Command cmd = commandManager.getCommands().getOrDefault(arg.toLowerCase(), null);

        if (cmd == null) {
            for (Map.Entry<String, Command> alias : commandManager.getAliases().entrySet()) {
                if (alias.getKey().toLowerCase().equals(arg.toLowerCase())) { cmd = alias.getValue(); }
            }
        }
        if (cmd == null) {
            context.sendTranslate("help.command.doesntexist").queue();
            return false;
        }
        if (cmd.getPermLevel().getNumer() > UserUtil.getPermLevel(context.getMember()).getNumer()) cmd = null;

        EmbedBuilder eb = getUsage(context, cmd);
        context.send(eb.build()).queue();
        return true;
    }


    public static EmbedBuilder getUsage(CommandContext context) {
        return getUsage(context, null);
    }

    public static EmbedBuilder getUsage(CommandContext context, @Nullable Command command) {
        Command cmd;
        if (command == null) {
            cmd = context.getCommand();
        } else cmd = command;

        EmbedBuilder eb = new EmbedBuilder();
        StringBuffer desc = new StringBuffer();

        desc.append("```");

        desc.append(context.getTranslate("help.cmd.usage", context.getTranslate(cmd.getName() + ".usage"))).append("\n");
        desc.append(context.getTranslate("help.cmd.desc", context.getTranslate(cmd.getName() + ".opis"))).append("\n");
        desc.append(context.getTranslate("help.cmd.category",
                context.getTranslate("category." + cmd.getCategory().name().toLowerCase()))).append("\n");
        desc.append(context.getTranslate("help.cmd.perm",
                context.getTranslate(cmd.getPermLevel().getTranlsateKey()), cmd.getPermLevel().getNumer())).append("\n");
        if (cmd.getCooldown() != 0) {
            desc.append(context.getTranslate("help.cmd.cooldown", cmd.getCooldown())).append("\n");
        }

        if (!cmd.getAliases().isEmpty()) {
            String aliases = cmd.getAliases().toString().replace("[", "")
                    .replace("]", "");
            desc.append(context.getTranslate("help.cmd.aliases", aliases)).append("\n");
        }

        String key = cmd.getName() + ".pomoc";
        String dodatkowaPomoc = context.getTranslate(key);
        if (!dodatkowaPomoc.equals(".")) {
            eb.addField(context.getTranslate("help.addpomoc"), "```\n" + dodatkowaPomoc + "```", false);
        }

        desc.append("```");

        eb.setTitle(context.getTranslate("help.cmd.cmd", context.getPrefix(), cmd.getName()));
        eb.setDescription(desc.toString());
        eb.setColor(UserUtil.getColor(context.getMember()));
        return eb;
    }

}
