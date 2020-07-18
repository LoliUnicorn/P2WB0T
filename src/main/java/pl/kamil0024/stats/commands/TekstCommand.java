package pl.kamil0024.stats.commands;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

public class TekstCommand extends Command {

    private EventWaiter eventWaiter;

    public TekstCommand(EventWaiter eventWaiter) {
        name = "tekst";
        aliases.add("lyrics");

        permLevel = PermLevel.HELPER;

        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgsToString(0);
        if (context.getArgs().get(0) == null) throw new UsageException();

        try {
            JSONObject job = NetworkUtil.getJson("https://some-random-api.ml/lyrics?title=" + NetworkUtil.encodeURIComponent(arg));

            String tytul = Objects.requireNonNull(job).getString("title");
            String author = Objects.requireNonNull(job).getString("author");
            String lyrics = Objects.requireNonNull(job).getString("lyrics");
            JSONObject thumbnail = job.getJSONObject("thumbnail");
            JSONObject links = job.getJSONObject("links");

            ArrayList<EmbedBuilder> pages = new ArrayList<>();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(UserUtil.getColor(context.getMember()));
            eb.addField("Tytuł", String.format("[%s](%s)", tytul, links.getString("genius")), false);
            eb.addField("Autor", author, false);
            eb.setTimestamp(Instant.now());
            eb.setImage(thumbnail.getString("genius"));
            pages.add(eb);

            new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA()).create(context.getChannel());

            return true;
        } catch (Exception ignored) {}

        context.send("Wstąpił zewnętrzny błąd z API (lub nie znaleziono piosenki)!").queue();
        return false;
    }

}
