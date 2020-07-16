package pl.kamil0024.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import org.joda.time.Duration;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.EmbedPageintaor;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.TrackScheduler;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class QueueCommand extends Command {

    private MusicModule musicModule;
    private EventWaiter eventWaiter;

    public QueueCommand(MusicModule musicModule, EventWaiter eventWaiter) {
        name = "queue";
        permLevel = PermLevel.HELPER;

        this.musicModule = musicModule;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        TrackScheduler trackScheduler = musicModule.getGuildAudioPlayer(context.getGuild()).getScheduler();
        if (trackScheduler == null || trackScheduler.getQueue().isEmpty()) {
            context.send("Nic nie gram!").queue();
            return false;
        }

        ArrayList<EmbedBuilder> pages = new ArrayList<>();
        for (AudioTrack audioTrack : trackScheduler.getQueue()) {
            pages.add(getEmbed(audioTrack));
        }
        new EmbedPageintaor(pages, context.getUser(), eventWaiter, context.getJDA()).create(context.getChannel());
        return true;
    }

    public static EmbedBuilder getEmbed(AudioTrack audioTrack) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.cyan);
        eb.setImage(getImageUrl(audioTrack));

        eb.addField("Tytuł", audioTrack.getInfo().title, false);
        eb.addField("Autor", audioTrack.getInfo().author, false);

        eb.addField("Długość", longToTimespan(audioTrack.getDuration()), false);

        return eb;
    }

    public static String getImageUrl(AudioTrack audtioTrack) {
        return String.format("https://i.ytimg.com/vi_webp/%s/sddefault.webp", audtioTrack.getIdentifier());
    }

    public static String longToTimespan(long lonk) {
        Duration dur = new Duration(lonk);
        StringBuilder sb = new StringBuilder();

        if (dur.getStandardDays() != 0) sb.append(dur.getStandardDays()).append(" d. ");
        if (dur.getStandardHours() != 0) sb.append(dur.getStandardDays()).append(" godz. ");
        if (dur.getStandardMinutes() != 0) sb.append(dur.getStandardDays()).append(" min. ");
        if (dur.getStandardSeconds() != 0) sb.append(dur.getStandardDays()).append(" sek. ");

        return sb.toString();
    }

}
