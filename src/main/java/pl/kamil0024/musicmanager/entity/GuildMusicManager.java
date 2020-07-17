package pl.kamil0024.musicmanager.entity;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.Data;
import net.dv8tion.jda.api.managers.AudioManager;


@Data
public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public final AudioManager audioManager;

    public boolean destroy = false;

    public GuildMusicManager(AudioPlayerManager manager, AudioManager audioManager) {
        this.audioManager = audioManager;
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player, audioManager, this);
        player.addListener(scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}