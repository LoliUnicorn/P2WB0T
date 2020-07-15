package pl.kamil0024.commands.system;

import com.google.inject.Inject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.RemindDao;
import pl.kamil0024.core.database.config.RemindConfig;
import pl.kamil0024.core.util.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemindmeCommand extends Command {

    @Inject private RemindDao remindDao;
    @Inject private EventWaiter eventWaiter;

    public RemindmeCommand(RemindDao remindDao, EventWaiter eventWaiter) {
        name = "remind";
        aliases.add("remindme");
        permLevel = PermLevel.HELPER;
        this.remindDao = remindDao;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();

        if (arg.toLowerCase().equals("list")) {
            List<RemindConfig> rc = remindDao.getAll();
            rc.removeIf(m -> !m.getUserId().equals(context.getUser().getId()));
            if (rc.isEmpty()) {
                context.send(context.getTranslate("remind.remindlist")).queue();
                return false;
            }
            ArrayList<EmbedBuilder> pages = new ArrayList<>();

            for (RemindConfig conf : rc) {
                pages.add(getEmbed(conf).setColor(UserUtil.getColor(context.getMember())));
            }
            new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA()).create(context.getChannel());
            return true;
        }

        Long dur = new Duration().parseLong(arg);
        if (dur == null) {
            context.send(context.getTranslate("cytuj.badtime")).queue();
            return false;
        }

        String tresc = context.getArgsToString(1);
        if (tresc == null) {
            context.send(context.getTranslate("remind.blank")).queue();
            return false;
        }
        RemindConfig rc = remindDao.get(remindDao.getNextId());
        rc.setUserId(context.getUser().getId());
        rc.setCzas(dur);
        rc.setTresc(tresc);
        rc.setMsg(context.getMessage().getJumpUrl());
        remindDao.save(rc);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy `@` HH:mm:ss");
        context.send(context.getTranslate("remind.save", sdf.format(new Date(dur)))).queue();
        return true;
    }

    private EmbedBuilder getEmbed(RemindConfig remind) {
        long dzisiaj = new Date().getTime();
        BDate bd = new BDate(dzisiaj, ModLog.getLang());
        EmbedBuilder eb = new EmbedBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        eb.addField("Treść przypomnienia", remind.getMsg(), false);
        eb.addField("Zaplanowano na", bd.difference(remind.getCzas()), false);
        eb.addField("Dzień zaplanowania", sdf.format(new Date(remind.getCzas())), false);
        eb.setColor(Color.cyan);
        return eb;
    }

    public static void check(RemindDao remindDao, ShardManager api) {
        long teraz = new Date().getTime();
        for (RemindConfig remind : remindDao.getAll()) {
            assert remind.getCzas() - teraz <= 0;

            BetterStringBuilder sb = new BetterStringBuilder();
            sb.appendLine("⏰ **Przypomnienie**: " + remind.getTresc());
            sb.appendLine("Przypomnienie ustawione w tej wiadomości:");
            sb.append(remind.getMsg());

            try {
                User u = api.retrieveUserById(remind.getUserId()).complete();
                u.openPrivateChannel().complete().sendMessage(sb.build()).queue();
            } catch (Exception ignored) {}

            remindDao.remove(remind);
        }

    }

}
