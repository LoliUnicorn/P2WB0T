package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.util.EmbedPageintaor;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Kara;
import pl.kamil0024.core.util.kary.KaryEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryCommand extends Command {

    private final CaseDao caseDao;
    private final EventWaiter eventWaiter;

    public HistoryCommand(CaseDao caseDao, EventWaiter eventWaiter) {
        name = "history";
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
        this.caseDao = caseDao;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        User u = context.getParsed().getUser(context.getArgs().get(0));
        if (u == null) {
            context.send("Nie ma takiego użytkownika!").queue();
            return false;
        }

        List<CaseConfig> cc = caseDao.getAll();
//        List<Kara> kary = cc.getCases().stream().filter(k -> k.getKaranyId().equals(u.getId())).collect(Collectors.toList());
        List<CaseConfig> kary = new ArrayList<>();
        for (CaseConfig tak : cc) {
            if (tak.getKara().getKaranyId().equals(u.getId())) kary.add(tak);
        }

        int banow = 0;
        int unbanow = 0;
        int mutow = 0;
        int unmutow = 0;
        int kickow = 0;
        int tempbanow = 0;
        int tempmutow = 0;
        for (CaseConfig k : kary) {
            Kara kara = k.getKara();
            if (kara.getTypKary() == KaryEnum.BAN) banow++;
            if (kara.getTypKary() == KaryEnum.UNBAN) unbanow++;
            if (kara.getTypKary() == KaryEnum.MUTE) mutow++;
            if (kara.getTypKary() == KaryEnum.KICK) kickow++;
            if (kara.getTypKary() == KaryEnum.TEMPBAN) tempbanow++;
            if (kara.getTypKary() == KaryEnum.TEMPMUTE) tempmutow++;
            if (kara.getTypKary() == KaryEnum.UNMUTE) unmutow++;
        }
        List<EmbedBuilder> pages = new ArrayList<>();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setThumbnail(u.getAvatarUrl());
        eb.setDescription("Historia kar użytkownika " + UserUtil.getLogName(u));
        eb.setColor(UserUtil.getColor(context.getMember()));

        eb.addField("Tempbanów", tempbanow + "", true);
        eb.addField("Banów", banow + "", true);
        eb.addField("Unbanów", unbanow + "", true);
        eb.addField("Mutów", mutow + "", false);
        eb.addField("Tempmutów", tempmutow + "", true);
        eb.addField("Unmutów", unmutow + "", true);
        eb.addField("Kicków", kickow + "", true);
        pages.add(eb);

        List<EmbedBuilder> historiaKar = new ArrayList<>();
        for (CaseConfig kara : kary) {
            EmbedBuilder ebb = ModLog.getEmbed(kara.getKara(), context.getShardManager());
            boolean aktywna = kara.getKara().getAktywna() == null ? false : kara.getKara().getAktywna();
            ebb.addField("Aktywna?", aktywna ? "Tak" : "Nie", false);
            historiaKar.add(ebb);
        }

        Collections.reverse(historiaKar);
        pages.addAll(historiaKar);
        new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA()).create(context.getChannel());
        return true;
    }

}
