package pl.kamil0024.commands.moderation;

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
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;

        this.caseDao = caseDao;
    }

    @Override
    public boolean execute(CommandContext context) {
        Integer id = context.getParsed().getNumber(context.getArgs().get(0));
        if (id == null) throw new UsageException();

        CaseConfig cc = caseDao.get(id);
        if (cc.getKara() == null) {
            context.send(context.getTranslate("karainfo.invalid")).queue();
            return false;
        }
        context.send(ModLog.getEmbed(cc.getKara(), context.getShardManager()).build()).queue();
        return true;
    }

}
