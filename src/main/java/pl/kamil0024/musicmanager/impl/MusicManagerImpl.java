package pl.kamil0024.musicmanager.impl;

import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import pl.kamil0024.musicmanager.MusicManager;

import javax.sound.midi.VoiceStatus;

@Data
public class MusicManagerImpl implements MusicManager {

    private JDA api;
    private Guild guild;

    public MusicManagerImpl(JDA api, Guild guild) {
        this.api = api;
        this.guild = guild;
    }

    @Override
    public void connect(VoiceChannel vc) {
        if (vc == null) throw new UnsupportedOperationException("Kanał głosowy jest nullem!");
        guild.getAudioManager().openAudioConnection(vc);
    }

    @Override
    public void connect(GuildVoiceState vs) {
        if (vs == null || vs.getChannel() == null) throw new UnsupportedOperationException("Kanał głosowy jest nullem!");
        guild.getAudioManager().openAudioConnection(vs.getChannel());
    }

    @Override
    public void close() {
        guild.getAudioManager().closeAudioConnection();
    }

}
