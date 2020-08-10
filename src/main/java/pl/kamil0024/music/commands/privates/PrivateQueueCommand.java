package pl.kamil0024.music.commands.privates;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.EmbedPageintaor;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.music.commands.PlayCommand;
import pl.kamil0024.music.commands.QueueCommand;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static pl.kamil0024.music.commands.QueueCommand.longToTimespan;

@SuppressWarnings("DuplicatedCode")
public class PrivateQueueCommand extends Command {

    private final MusicAPI musicAPI;
    private final EventWaiter eventWaiter;

    public PrivateQueueCommand(MusicAPI musicAPI, EventWaiter eventWaiter) {
        name = "pqueue";
        aliases.add("privatequeue");
        category = CommandCategory.PRIVATE_CHANNEL;
        this.musicAPI = musicAPI;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!PrivatePlayCommand.check(context)) return false;

        int wolnyBot = 0;
        MusicRestAction restAction = null;

        for (Member member : PlayCommand.getVc(context.getMember()).getMembers()) {
            if (member.getUser().isBot()) {
                Integer agent = musicAPI.getPortByClient(member.getId());
                if (agent != null) {
                    wolnyBot = agent;
                    restAction = musicAPI.getAction(agent);
                }

            }
        }

        if (wolnyBot == 0) {
            context.send("Na Twoim kanale nie ma żadnego bota").queue();
            return false;
        }

        try {
            List<EmbedBuilder> traki = new ArrayList<>();

            MusicResponse skip = restAction.getQueue();
            MusicResponse playing = restAction.getPlayingTrack();
            if ((skip.isError() && !skip.getError().getDescription().contains("Kolejka jest pusta!") ) || playing.isError()) {
                context.send("Wystąpił błąd: " + skip.getError().getDescription()).queue();
                return false;
            }

            Iterator<Object> jsona = skip.json.getJSONArray("data").iterator();

            String json = playing.json.getJSONObject("data").toString();
            Track played = new Gson().fromJson(json, Track.class);
            if (played != null) {
                traki.add(new DecodeTrack(json, true).create());
            }

            while (jsona.hasNext()) {
                traki.add(new DecodeTrack(jsona.next().toString(), false).create());
            }

            new EmbedPageintaor(traki, context.getUser(), eventWaiter, context.getJDA()).create(context.getChannel());
            return true;
        } catch (Exception e) {
            context.send("Wystąpił błąd: " + e.getLocalizedMessage()).queue();
            return false;
        }
    }

    public class DecodeTrack {

        private final String json;
        private final Track trak;
        private final boolean aktualnieGrana;

        public DecodeTrack(String string, boolean aktualnieGrana) {
            this.json = string;
            this.trak = new Gson().fromJson(json, Track.class);
            this.aktualnieGrana = aktualnieGrana;
        }

        public EmbedBuilder create() {
            EmbedBuilder eb = new EmbedBuilder();

            eb.setColor(Color.cyan);
            eb.setImage(QueueCommand.getImageUrl(trak.getIdentifier()));

            eb.addField("Tytuł", String.format("[%s](%s)", trak.getTitle(), QueueCommand.getYtLink(trak.getIdentifier())), false);
            eb.addField("Autor", trak.getAuthor(), false);

            if (!aktualnieGrana) {
                eb.addField("Długość", trak.isStream() ? "To jest stream ;p" : longToTimespan(trak.getLength()), true);
            } else {
                eb.addField("Długość",  longToTimespan(trak.getLength()), true);
                eb.addField("Pozostało", longToTimespan(trak.getLength() - trak.getPosition()), false);
            }

            return eb;
        }

    }

    @Data
    @AllArgsConstructor
    public static class Track {

        private final String identifier;
        private final String author;
        private final String title;
        private final boolean stream;
        private final long length;
        private final long position;

    }

}
