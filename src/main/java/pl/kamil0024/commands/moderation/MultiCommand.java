package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.database.config.MultiConfig;
import pl.kamil0024.core.util.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MultiCommand extends Command {

    private final MultiDao multiDao;
    private final EventWaiter eventWaiter;

    public MultiCommand(MultiDao multiDao, EventWaiter eventWaiter) {
        name = "multi";
        permLevel = PermLevel.HELPER;

        this.multiDao = multiDao;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        User user = context.getParsed().getUser(context.getArgs().get(0));
        if (user == null) throw new UsageException();

        Message msg = context.send("Ładuje...").complete();

        MultiConfig mc = multiDao.get(context.getUser().getId());
        if (mc.getNicki().isEmpty()) {
            msg.editMessage("Te konto nie ma kont!").queue();
            return false;
        }

        ArrayList<EmbedBuilder> pages = new ArrayList<>();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));

        BetterStringBuilder sb = new BetterStringBuilder();
        sb.appendLine("```");

        for (Nick nick : mc.getNicki()) {
            sb.appendLine(format(nick));
            if (sb.toString().length() >= 1900) {
                sb.append("```");
                eb.setDescription(sb.toString());
                pages.add(eb);

                eb = new EmbedBuilder();
                eb.setColor(UserUtil.getColor(context.getMember()));

                sb = new BetterStringBuilder();
                sb.appendLine("```");
            }
        }

        if (pages.isEmpty()) {
            sb.append("```");
            eb.setDescription(sb.toString());
            pages.add(eb);
        }

        new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA()).create(msg);

        return true;
    }

    public static String format(Nick nick) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy @ HH:mm:ss");
        return sfd.format(new Date(nick.getDate())) + " - " + nick.getNick();
    }

}
