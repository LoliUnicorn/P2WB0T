package pl.kamil0024.musicbot.music.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicManager {

    private final ShardManager api;

    private final AudioPlayerManager playerManager;
    @Getter public final Map<Long, GuildMusicManager> musicManagers;

    private static YoutubeSearchProvider youtubeSearchProvider = new YoutubeSearchProvider();
    public YoutubeAudioSourceManager youtubeSourceManager;

    public MusicManager(ShardManager api) {
        this.api = api;
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        this.youtubeSourceManager = new YoutubeAudioSourceManager(true);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.registerSourceManager(youtubeSourceManager);
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null || musicManager.getPlayer() == null || musicManager.getDestroy()) {
            musicManager = new GuildMusicManager(playerManager, guild.getAudioManager());
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public boolean loadAndPlay(final Guild guild, final String trackUrl, VoiceChannel vc) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        AtomicBoolean error = new AtomicBoolean(false);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(guild, musicManager, track, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    play(guild, musicManager, track, vc);
                }
            }
            @Override
            public void noMatches() {
                error.set(true);
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                error.set(true);
            }
        });
        return !error.get();
    }

    public synchronized void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel vc) {
        for (AudioTrack audioTrack : musicManager.getQueue()) {
            if (audioTrack.getIdentifier().equals(track.getIdentifier())) {
                return;
            }
        }

        connectToFirstVoiceChannel(guild.getAudioManager(), vc);
        musicManager.queue(track);

        if (musicManager.getAktualnaPiosenka() == null) {
            musicManager.setAktualnaPiosenka(track);
        }

    }

    public static void connectToFirstVoiceChannel(AudioManager audioManager, VoiceChannel vc) {
        audioManager.openAudioConnection(vc);
    }

    public List<AudioTrack> search(String tytul) {
        List<AudioTrack> results = new ArrayList<>();
        AudioItem playlist = youtubeSearchProvider.loadSearchResult(tytul, info -> new YoutubeAudioTrack(info, youtubeSourceManager));

        if (playlist instanceof AudioPlaylist) {
            results.addAll(((AudioPlaylist) playlist).getTracks());
        }

        return results;
    }

    public static EmbedBuilder getEmbed(AudioTrack audioTrack, boolean aktualnieGrana) {
        EmbedBuilder eb = new EmbedBuilder();
        AudioTrackInfo info = audioTrack.getInfo();

        eb.setColor(Color.cyan);
        eb.setImage(getImageUrl(audioTrack));

        eb.addField("Tytuł", String.format("[%s](%s)", info.title, getYtLink(audioTrack)), false);
        eb.addField("Autor", info.author, false);

        if (!aktualnieGrana) {
            eb.addField("Długość", info.isStream ? "To jest stream ;p" : longToTimespan(info.length), true);
        } else {
            eb.addField("Długość",  longToTimespan(info.length), true);
            eb.addField("Pozostało", longToTimespan(info.length - audioTrack.getPosition()), false);
        }

        return eb;
    }

    public static String getImageUrl(AudioTrack audtioTrack) {
        return String.format("https://i.ytimg.com/vi_webp/%s/sddefault.webp", audtioTrack.getIdentifier());
    }

    public static String getYtLink(AudioTrack audioTrack) {
        return String.format("https://www.youtube.com/watch?v=%s", audioTrack.getIdentifier());
    }

    public static String longToTimespan(Number milins) {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(milins.longValue()), ZoneId.of("GMT")));
    }

}
