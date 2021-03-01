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

package pl.kamil0024.musicbot.music.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.planner.BalancingIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.socket.SocketClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicManager {

    private final ShardManager api;
    private final AudioPlayerManager playerManager;
    public final SocketClient socketClient;

    @Getter public final Map<Long, GuildMusicManager> musicManagers;

    public YoutubeAudioSourceManager youtubeSourceManager;

    public MusicManager(ShardManager api) {
        this.api = api;
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        this.youtubeSourceManager = new YoutubeAudioSourceManager(true);
        this.socketClient = new SocketClient(this, api);

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.registerSourceManager(youtubeSourceManager);

        try {
            AbstractRoutePlanner planner = new BalancingIpRoutePlanner(Collections.singletonList(new Ipv6Block(Ustawienia.instance.api.cidr)));
            new YoutubeIpRotatorSetup(planner)
                    .forSource(playerManager.source(YoutubeAudioSourceManager.class))
                    .setup();
        } catch (Exception ignored) { }

    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null || musicManager.getPlayer() == null || musicManager.getDestroy()) {
            musicManager = new GuildMusicManager(playerManager, guild.getAudioManager(), socketClient);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public synchronized void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, @Nullable VoiceChannel vc) {
        for (AudioTrack audioTrack : musicManager.getQueue()) {
            if (audioTrack.getIdentifier().equals(track.getIdentifier())) {
                return;
            }
        }

        if (vc != null) {
            guild.getAudioManager().openAudioConnection(vc);
        }

        musicManager.queue(track);

        if (musicManager.getAktualnaPiosenka() == null) {
            musicManager.setAktualnaPiosenka(track);
        }

    }

}
