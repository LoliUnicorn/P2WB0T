package pl.kamil0024.musicmanager;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.VoiceStatus;

public interface MusicManager {

    void connect(@Nullable VoiceChannel vc);

    void connect(@Nullable GuildVoiceState vs);

    void close();

}
