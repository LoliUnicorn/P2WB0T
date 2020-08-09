package pl.kamil0024.musicbot.music.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.kamil0024.musicbot.music.managers.entity.AudioPlayerSendHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GuildMusicManager extends AudioEventAdapter {

    @Getter public AudioPlayer player;
    @Getter public AudioManager audioManager;
    @Getter public AudioPlayerManager manager;

    @Getter public final BlockingQueue<AudioTrack> queue;

    @Getter @Setter public AudioTrack aktualnaPiosenka = null;
    @Getter @Setter public Boolean destroy = false;

    public GuildMusicManager(AudioPlayerManager manager, AudioManager audioManager) {
        this.audioManager = audioManager;
        this.queue = new LinkedBlockingQueue<>();
        this.manager = manager;
        player = this.manager.createPlayer();
        player.addListener(this);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public void queue(AudioTrack track) {
        if (queue.size() > 10) return;

        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }

    }

    public void nextTrack() {
        AudioTrack next = queue.poll();

        if (getDestroy() || next == null) {
            destroy();
            return;
        }

        player.startTrack(next, false);
        setAktualnaPiosenka(next);
    }

    public void destroy() {
        setDestroy(true);
        getPlayer().destroy();
        getPlayer().removeListener(this);
        getAudioManager().closeAudioConnection();
        setAktualnaPiosenka(null);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

}
