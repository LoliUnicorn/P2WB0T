package pl.kamil0024.musicmanager.entity;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.music.commands.QueueCommand;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    // haha @Data robi brrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr

    @Getter private AudioPlayer player;
    @Getter private AudioManager audioManager;
    @Getter private AudioPlayerManager manager;
    @Getter private GuildMusicManager guildMusicManager;

    @Getter public final BlockingQueue<AudioTrack> queue;

    @Getter @Setter public AudioTrack aktualnaPiosenka = null;
    @Getter @Setter public Boolean destroy = false;
    @Getter @Setter public Boolean loop = false;

    public TrackScheduler(AudioPlayer player, AudioManager audioManager, AudioPlayerManager manager, GuildMusicManager guildMusicManager) {
        this.player = player;
        this.audioManager = audioManager;
        this.manager = manager;
        this.queue = new LinkedBlockingQueue<>();
        this.guildMusicManager = guildMusicManager;
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        AudioTrack next = null;
        if (!getLoop()) {
            next = queue.poll();
        }

        if (getDestroy() || (next == null && !getLoop())) {
            Log.debug("Destroy");
            destroy();
            return;
        }

        if (getLoop()) {
            manager.loadItemOrdered(guildMusicManager, QueueCommand.getYtLink(getAktualnaPiosenka()), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    setAktualnaPiosenka(track);
                    player.startTrack(track, false);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {}
                @Override
                public void noMatches() {}
                @Override
                public void loadFailed(FriendlyException exception) {}

            });
            return;
        }

        if (next != null) {
            player.startTrack(next, false);
            setAktualnaPiosenka(next);
        }

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
