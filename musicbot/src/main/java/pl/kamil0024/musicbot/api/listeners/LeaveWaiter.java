/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
                    if (LeaveVcListener.leave(vc)) {
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
