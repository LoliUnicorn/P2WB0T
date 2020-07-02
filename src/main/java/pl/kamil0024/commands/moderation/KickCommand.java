package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import java.util.Date;

import static pl.kamil0024.core.util.kary.Kara.check;

@SuppressWarnings("DuplicatedCode")
public class KickCommand extends Command {

    private final CaseDao caseDao;
    private final ModLog modLog;

    public KickCommand(CaseDao caseDao, ModLog modLog) {
        name = "kick";
        aliases.add("wyrzuc");
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
        this.caseDao = caseDao;
        this.modLog = modLog;
    }

    @Override
    public boolean execute(CommandContext context) {
        Member mem = context.getParsed().getMember(context.getArgs().get(0));
        if (mem == null) {
            context.sendTranslate("kick.badmember");
            return false;
        }
        String powod = context.getArgsToString(1);
        if (powod == null) powod = context.getTranslate("modlog.none");

        String check = check(context, mem.getUser());
        if (check != null) {
            context.send(check).queue();
            return false;
        }

        Message msg = context.send("Ładuje...").complete();
        String nick = UserUtil.getMcNick(mem);
        try {
            context.getGuild().kick(mem, powod).complete();
        } catch (InsufficientPermissionException e) {
            msg.editMessage(context.getTranslate("kick.permex")).queue();
            return false;
        } catch (HierarchyException e) {
            msg.editMessage(context.getTranslate("kick.hierarchyex")).queue();
            return false;
        }

        msg.editMessage(context.getTranslate("kick.succes", UserUtil.getLogName(mem), powod)).queue();

        Kara kara = new Kara();
        kara.setKaranyId(mem.getId());
        kara.setMcNick(nick);
        kara.setAdmId(context.getUser().getId());
        kara.setPowod(powod);
        kara.setTimestamp(new Date().getTime());
        kara.setTypKary(KaryEnum.KICK);
        Kara.put(caseDao, kara, modLog);

        return true;
    }

}
