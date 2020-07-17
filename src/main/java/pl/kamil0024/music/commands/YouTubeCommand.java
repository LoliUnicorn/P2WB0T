package pl.kamil0024.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.BetterStringBuilder;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YouTubeCommand extends Command {

    private MusicModule musicModule;
    private EventWaiter eventWaiter;

    public YouTubeCommand(MusicModule musicModule, EventWaiter eventWaiter) {
        name = "youtube";
        aliases.add("yt");
        permLevel = PermLevel.HELPER;
        this.musicModule = musicModule;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        String tytul = context.getArgsToString(0);
        if (context.getArgs().get(0) == null) throw new UsageException();
        List<AudioTrack> audioTrackList = musicModule.search(tytul);

        if (audioTrackList.isEmpty()) {
            context.send("Nie znaleziono dopasowań!").queue();
            return false;
        }
        HashMap<Integer, AudioTrack> mapa = new HashMap<>();

        BetterStringBuilder bsb = new BetterStringBuilder();
        bsb.appendLine("```");
        bsb.appendLine("= Napisz liczby utworów, których chcesz posłuchać = ");
        bsb.appendLine("PS: Można wpisać 1,2,3");
        int tracks = 0;
        for (AudioTrack audioTrack : audioTrackList) {
            tracks++;
            AudioTrackInfo info = audioTrack.getInfo();
            bsb.appendLine(tracks + ". " + info.title + " :: " + info.author);
            mapa.put(tracks, audioTrack);
            if (tracks == 10) break;
        }
        bsb.appendLine("```");

        Message msg = context.send(bsb.toString()).complete();

        eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                (event) -> event.getAuthor().getId().equals(context.getUser().getId()) && event.getChannel().getId().equals(context.getChannel().getId()),
                (event) -> {
                    List<Integer> lista = new ArrayList<>();
                    String eMsg = event.getMessage().getContentRaw().replaceAll(" ", "");
                    for (String s : eMsg.split(",")) {
                        Integer i = context.getParsed().getNumber(s);
                        assert i != null && mapa.get(i) != null;
                        lista.add(i);
                    }
                    if (lista.isEmpty()) {
                        msg.delete().complete();
                        return;
                    }
                    msg.delete().complete();
                    lista.forEach(i -> musicModule.loadAndPlay(context.getChannel(), QueueCommand.getYtLink(mapa.get(i)), PlayCommand.getVc(context.getMember())));
                    context.send("Dodano " + lista.size() + " utworów do kolejki!").queue();
                }, 30, TimeUnit.SECONDS, () -> msg.delete().queue());

        return true;
    }



}
