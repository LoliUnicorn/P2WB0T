package pl.kamil0024.musicbot.api.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import pl.kamil0024.musicbot.core.util.EventWaiter;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LeaveWaiter {

    private EventWaiter eventWaiter;
    private MusicManager musicManager;

    private List<String> czekaja;

    public LeaveWaiter(EventWaiter eventWaiter, MusicManager musicManager) {
        this.eventWaiter = eventWaiter;
        this.musicManager = musicManager;
        this.czekaja = new ArrayList<>();
    }

    public void initWaiter(VoiceChannel vc) {
        if (czekaja.contains(vc.getGuild().getId())) return;

        czekaja.add(vc.getGuild().getId());
        eventWaiter.waitForEvent(GuildVoiceJoinEvent.class, this::checkJoin, this::event,
                1, TimeUnit.MINUTES,
                () -> {
                    List<Member> members = vc.getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toList());
                    int size = members.size();
                    if (size < 4) {
                        musicManager.getGuildAudioPlayer(vc.getGuild()).destroy();
                    }
                    czekaja.remove(vc.getGuild().getId());
                }
        );
    }

    public boolean checkJoin(GuildVoiceJoinEvent event) {
        return czekaja.contains(event.getChannelJoined().getId());
    }

    public void event(GuildVoiceJoinEvent event) {
        czekaja.remove(event.getChannelJoined().getId());
    }

}
