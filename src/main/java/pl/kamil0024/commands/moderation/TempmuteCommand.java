package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;
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
import java.util.Objects;

import static pl.kamil0024.core.util.kary.Kara.check;

@SuppressWarnings("DuplicatedCode")
public class TempmuteCommand extends Command {

    private final CaseDao caseDao;
    private final ModLog modLog;

    public TempmuteCommand(CaseDao caseDao, ModLog modLog) {
        name = "tempmute";
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
        this.caseDao = caseDao;
        this.modLog = modLog;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        Member mem = context.getParsed().getMember(context.getArgs().get(0));
        if (mem == null) {
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

        String check = check(context, mem.getUser());
        if (check != null) {
            context.send(check).queue();
            return false;
        }
        String error = tempmute(mem, context.getUser(), powod, duration, caseDao, modLog, false);
        if (error != null) {
            context.send("Wysąpił błąd! " + error).queue();
            return false;
        }
        context.sendTranslate("tempmute.succes", UserUtil.getLogName(mem), powod, duration).queue();
        return true;
    }

    public static String tempmute(Member user, User adm, String powod, String duration, CaseDao caseDao, ModLog modLog, boolean isPun) {
        if (UserUtil.getPermLevel(adm).getNumer() <= UserUtil.getPermLevel(user).getNumer()) {
            return "Poziom uprawnień osoby, którą chcesz ukarać jest wyższy od Twojego!";
        }
        Long dur = new Duration().parseLong(duration);
        if (dur == null) {
            return "Duration " + duration + " jest zły!";
        }

        if (MuteCommand.hasMute(user)) {
            return "Ta osoba jest już wyciszona!";
        }

        Role r = user.getGuild().getRoleById(Ustawienia.instance.muteRole);

        user.getGuild().addRoleToMember(user, Objects.requireNonNull(r)).complete();
        try {
            user.getGuild().kickVoiceMember(user).queue();
        } catch (Exception ignored) {}

        if (!isPun) {
            Kara kara = new Kara();
            kara.setKaranyId(user.getId());
            kara.setMcNick(UserUtil.getMcNick(user));
            kara.setAdmId(adm.getId());
            kara.setPowod(powod);
            kara.setTimestamp(new Date().getTime());
            kara.setTypKary(KaryEnum.TEMPMUTE);
            kara.setEnd(dur);
            kara.setDuration(duration);
            Kara.put(caseDao, kara, modLog);
        }

        return null;
    }

}
