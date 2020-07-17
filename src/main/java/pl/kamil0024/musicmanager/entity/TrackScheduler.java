package pl.kamil0024.musicmanager.entity;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.kamil0024.core.logger.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@EqualsAndHashCode(callSuper = true)
@Data
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private AudioManager audioManager;

    public final BlockingQueue<AudioTrack> queue;

    public AudioTrack aktualnaPiosenka = null;

    public TrackScheduler(AudioPlayer player, AudioManager audioManager) {
        this.player = player;
        this.audioManager = audioManager;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        AudioTrack next = queue.poll();
        if (next != null) {
            player.startTrack(next, false);
            setAktualnaPiosenka(next);
        } else {
            getAudioManager().closeAudioConnection();
            setAktualnaPiosenka(null);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

}
