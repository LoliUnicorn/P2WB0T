package pl.kamil0024.core.database.config;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.concurrent.BlockingQueue;

@Table("voicestate")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class VoiceStateConfig {
    public VoiceStateConfig() {}

    public VoiceStateConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id;

    private VoiceChannel voiceChannel = null;
    BlockingQueue<AudioTrack> queue = null;
    AudioTrack aktualnaPiosenka = null;

}
