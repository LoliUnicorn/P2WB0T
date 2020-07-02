package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import java.util.Date;
import java.util.Objects;

public class UnmuteCommand extends Command {

    private final CaseDao caseDao;
    private final ModLog modLog;

    public UnmuteCommand(CaseDao caseDao, ModLog modLog) {
        name = "unmute";
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

        if (!MuteCommand.hasMute(mem)) {
            context.send("Taki użytkownik nie jest wyciszony!");
            return false;
        }
        Role r = mem.getGuild().getRoleById(Ustawienia.instance.muteRole);
        String powod = context.getArgsToString(1);
        if (powod == null) {
            context.send("Musisz podać powód!").queue();
            return false;
        }
        try {
            context.getGuild().removeRoleFromMember(mem, Objects.requireNonNull(r)).complete();
            context.sendTranslate("unmute.succes", UserUtil.getName(mem.getUser()), powod).queue();

            for (CaseConfig kara1 : caseDao.getAktywe(mem.getId())) {
                if (kara1.getKara().getTypKary() == KaryEnum.MUTE || kara1.getKara().getTypKary() == KaryEnum.TEMPMUTE) {
                    kara1.getKara().setAktywna(false);
                    caseDao.save(kara1);
                }
            }

            Kara kara = new Kara();
            kara.setKaranyId(mem.getId());
            kara.setMcNick(UserUtil.getMcNick(mem));
            kara.setAdmId(context.getUser().getId());
            kara.setPowod(powod);
            kara.setTimestamp(new Date().getTime());
            kara.setTypKary(KaryEnum.UNMUTE);
            Kara.put(caseDao, kara, modLog);
            return true;
        } catch (Exception e) {
            context.send("Nie udało się odcyszyć użytkownika! " + e.getMessage());
            return false;
        }
    }

}
