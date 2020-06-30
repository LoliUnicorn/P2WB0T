package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.entities.Member;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UserUtil;

public class PoziomCommand extends Command {

    public PoziomCommand() {
        name = "poziom";
        category = CommandCategory.SYSTEM;
    }

    @Override
    public boolean execute(CommandContext context) {

        Member mem = context.getParsed().getMember(context.getArgs().get(0));
        if (mem == null) mem = context.getMember();

        PermLevel lvl = UserUtil.getPermLevel(mem);
        context.sendTranslate("poziom.send", UserUtil.getName(mem.getUser()),
                lvl.getNumer(), context.getTranslate(lvl.getTranlsateKey())).queue();
        return true;
    }

}
