package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;
import pl.kamil0024.stats.StatsModule;

import java.util.Date;

import static pl.kamil0024.core.util.kary.Kara.check;

@SuppressWarnings("DuplicatedCode")
public class BanCommand extends Command {

    private final CaseDao caseDao;
    private final ModLog modLog;
    private final StatsModule statsModule;

    public BanCommand(CaseDao caseDao, ModLog modLog, StatsModule statsModule) {
        name = "ban";
        aliases.add("akysz");
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
        this.caseDao = caseDao;
        this.modLog = modLog;
        this.statsModule = statsModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        User mem = context.getParsed().getUser(context.getArgs().get(0));
        if (mem == null) {
            context.sendTranslate("kick.badmember");
            return false;
        }
        String powod = context.getArgsToString(1);
        if (powod == null) powod = context.getTranslate("modlog.none");
        String check = check(context, mem);
        if (check != null) {
            context.send(check).queue();
            return false;
        }

        Message msg = context.send("Ładuje...").complete();
        String nick = UserUtil.getMcNick(context.getGuild().getMember(mem));

        Member m = context.getGuild().getMember(mem);
        if (m != null && !context.getGuild().getSelfMember().canInteract(m)) {
            msg.editMessage(context.getTranslate("ban.error")).queue();
            return false;
        }

        msg.editMessage(context.getTranslate("ban.succes", UserUtil.getLogName(mem), powod)).queue();

        Kara kara = new Kara();
        kara.setKaranyId(mem.getId());
        kara.setMcNick(nick);
        kara.setAdmId(context.getUser().getId());
        kara.setPowod(powod);
        kara.setTimestamp(new Date().getTime());
        kara.setTypKary(KaryEnum.BAN);
        Kara.put(caseDao, kara, modLog);

        context.getGuild().ban(mem, 0, powod).complete();
        statsModule.getStatsCache().addZbanowanych(context.getUser().getId(), 1);
        return true;
    }

}
