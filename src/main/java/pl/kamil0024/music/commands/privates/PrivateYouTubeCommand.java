package pl.kamil0024.music.commands.privates;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.BetterStringBuilder;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.music.commands.PlayCommand;
import pl.kamil0024.music.commands.QueueCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrivateYouTubeCommand extends Command {

    private final MusicAPI musicAPI;
    private final EventWaiter eventWaiter;
    private final MusicModule musicModule;

    public PrivateYouTubeCommand(MusicAPI musicAPI, EventWaiter eventWaiter, MusicModule musicModule) {
        name = "pyt";
        aliases.add("privateeyt");
        aliases.add("privateeyoutube");
        category = CommandCategory.PRIVATE_CHANNEL;
        permLevel = PermLevel.HELPER;
        this.musicAPI = musicAPI;
        this.eventWaiter = eventWaiter;
        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!PrivatePlayCommand.check(context)) return false;

        String tytul = context.getArgsToString(0);
        if (context.getArgs().get(0) == null) throw new UsageException();
        List<AudioTrack> audioTrackList = musicModule.search(tytul);

        if (audioTrackList.isEmpty()) {
            context.sendTranslate("youtube.bad").queue();
            return false;
        }
        HashMap<Integer, AudioTrack> mapa = new HashMap<>();

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

        if (wolnyBot == 0 && restAction == null) {
            for (Integer port : musicAPI.getPorts()) {
                restAction = musicAPI.getAction(port);
                if (restAction.getVoiceChannel() == null) {
                    wolnyBot = port;
                    try {
                        MusicResponse tak = restAction.connect(PlayCommand.getVc(context.getMember()));
                    } catch (Exception e) {
                        context.send("Nie udało się dołączyć na kanał głosowy.").queue();
                        return false;
                    }
                    break;
                }
            }
        }

        if (wolnyBot == 0) {
            context.send("Aktualnie nie ma wolnych botów.").queue();
            return false;
        }

        BetterStringBuilder bsb = new BetterStringBuilder();
        bsb.appendLine("```");
        bsb.appendLine(context.getTranslate("youtube.firstline"));
        int tracks = 0;
        for (AudioTrack audioTrack : audioTrackList) {
            tracks++;
            AudioTrackInfo info = audioTrack.getInfo();
            bsb.appendLine(tracks + ". " + info.title + " :: " + info.author);
            mapa.put(tracks, audioTrack);
            if (tracks == 10) break;
        }
        bsb.appendLine("```");

        try {
            Message msg = context.send(bsb.toString()).complete();

            MusicRestAction finalRestAction = restAction;
            eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                    (event) -> event.getAuthor().getId().equals(context.getUser().getId()) && event.getChannel().getId().equals(context.getChannel().getId()),
                    (event) -> {
                        List<Integer> lista = new ArrayList<>();
                        String eMsg = event.getMessage().getContentRaw().replaceAll(" ", "");
                        for (String s : eMsg.split(",")) {
                            Integer i = context.getParsed().getNumber(s);
                            if (i != null && mapa.get(i) != null) {
                                lista.add(i);
                            }
                        }
                        try {
                            if (lista.isEmpty()) {
                                if (finalRestAction.getQueue().isError() && finalRestAction.getPlayingTrack().isError()) {
                                    finalRestAction.disconnect();
                                }
                                msg.delete().complete();
                                return;
                            }
                            msg.delete().complete();
                            lista.forEach(i -> {
                                try {
                                    finalRestAction.play(QueueCommand.getYtLink(mapa.get(i)).split("v=")[1]);
                                } catch (IOException ignored) { }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            context.send("Wystąpił błąd: " + e.getLocalizedMessage()).queue();
                        }
                        context.sendTranslate("youtube.succes", lista.size()).queue();
                    }, 15, TimeUnit.SECONDS, () -> {
                        try {
                            if (finalRestAction.getQueue().isError() && finalRestAction.getPlayingTrack().isError()) {
                                finalRestAction.disconnect();
                            }
                        } catch (Exception ignored) {}
                        msg.delete().queue();
                    });
        } catch (Exception e) {
            context.send("Wystąpił błąd z API! " + e.getLocalizedMessage());
            Log.newError(e);
        }

        return true;
    }

}
