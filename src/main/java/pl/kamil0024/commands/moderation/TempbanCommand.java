package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.Duration;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import java.util.Date;

import static pl.kamil0024.core.util.kary.Kara.check;

@SuppressWarnings("DuplicatedCode")
public class TempbanCommand extends Command {

    private final CaseDao caseDao;
    private final ModLog modLog;

    public TempbanCommand(CaseDao caseDao, ModLog modLog) {
        name = "tempban";
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
        this.caseDao = caseDao;
        this.modLog = modLog;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        Member user = context.getParsed().getMember(context.getArgs().get(0));
        if (user == null) {
            context.sendTranslate("kick.badmember").queue();
            return false;
        }
        String duration = context.getArgs().get(1);
        String powod = context.getArgsToString(2);
        if (duration == null) {
            context.send("Podaj czas!").queue();
            return false;
        }
        if (powod == null) powod = context.getTranslate("modlog.none");

        String check = check(context, user.getUser());
        if (check != null) {
            context.send(check).queue();
            return false;
        }
        String error = tempban(user, context.getUser(), powod, duration, caseDao, modLog, false);
        if (error != null) {
            context.send("Wysąpił błąd! " + error).queue();
            return false;
        }
        context.sendTranslate("tempban.succes", UserUtil.getLogName(user), powod, duration).queue();
        return true;
    }

    public static String tempban(Member user, User adm, String powod, String duration, CaseDao caseDao, ModLog modLog, boolean isPun) {
        if (UserUtil.getPermLevel(adm).getNumer() <= UserUtil.getPermLevel(user).getNumer()) {
            return "Poziom uprawnień osoby, którą chcesz ukarać jest wyższy od Twojego!";
        }
        Long dur = new Duration().parseLong(duration);
        if (dur == null) {
            return "Duration " + duration + " jest zły!";
        }

        for (Guild.Ban ban : user.getGuild().retrieveBanList().complete()) {
            if (ban.getUser().getId().equals(user.getId())) {
                return "Ta osoba jest już zbanowana!";
            }
        }

        String nick = UserUtil.getMcNick(user);

        if (!isPun) {
            Kara kara = new Kara();
            kara.setKaranyId(user.getId());
            kara.setMcNick(nick);
            kara.setAdmId(adm.getId());
            kara.setPowod(powod);
            kara.setTimestamp(new Date().getTime());
            kara.setTypKary(KaryEnum.TEMPBAN);
            kara.setEnd(dur);
            kara.setDuration(duration);
            Kara.put(caseDao, kara, modLog);
        }

        user.getGuild().ban(user, 0, powod).complete();
        return null;
    }

}
