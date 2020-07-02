package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.TimeUtil;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.DAYS;

public class ClearCommand extends Command {

    public ClearCommand() {
        name = "clear";
        aliases.add("purge");
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MODERATION;
    }

    @Override
    public boolean execute(CommandContext context) {
        User user = null;
        Integer liczba = null;
        TextChannel kanal = null;

        if (context.getParsed().getUser(context.getArgs().get(0)) == null) {
            liczba = context.getParsed().getNumber(context.getArgs().get(0));
            kanal = context.getParsed().getTextChannel(context.getArgs().get(1));

        } else {
            user = context.getParsed().getUser(context.getArgs().get(0));
            liczba = context.getParsed().getNumber(context.getArgs().get(1));
            kanal = context.getParsed().getTextChannel(context.getArgs().get(2));
        }
        if (kanal == null) kanal = context.getChannel();

        if (liczba == null || liczba > 100 || liczba <= 1) {
             context.send("Liczba wiadomości do usunięcia musi być między 2-100").queue();
             return true;
        }
        CompletableFuture<MessageHistory> historia;
        if (user == null) historia = kanal.getHistoryBefore(context.getMessage(), liczba).submit();
        else historia = kanal.getHistoryBefore(context.getMessage(), 100).submit();

        long dwaTygodnieTemu = (System.currentTimeMillis() - DAYS.toMillis(14) - TimeUtil.DISCORD_EPOCH) << TimeUtil.TIMESTAMP_OFFSET;
        List<Message> wiadomosciWszystkie = new ArrayList<>();
        List<Message> wiadomosci = historia.join().getRetrievedHistory();

        context.getMessage().delete().queue();
        for (Message msg : wiadomosci) {
            if (wiadomosciWszystkie.size() != liczba) {
                if (msg.getIdLong() > dwaTygodnieTemu) {
                    if (user != null && msg.getAuthor().getId().equals(user.getId())) {
                        wiadomosciWszystkie.add(msg);
                    } else if (user == null) wiadomosciWszystkie.add(msg);
                }
            }
        }
        kanal.purgeMessages(wiadomosciWszystkie);
        return true;
    }

}
