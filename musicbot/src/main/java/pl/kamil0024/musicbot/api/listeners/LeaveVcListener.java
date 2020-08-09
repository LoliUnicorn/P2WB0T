package pl.kamil0024.musicbot.api.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import javax.annotation.Nonnull;

public class LeaveVcListener extends ListenerAdapter {

    private final MusicManager musicManager;

    public LeaveVcListener(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getId().equals(Ustawienia.instance.bot.guildId)) {
            return;
        }
        musicManager.getMusicManagers().get(guild.getIdLong()).destroy();
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getId().equals(Ustawienia.instance.bot.guildId)) {
            return;
        }
        musicManager.getMusicManagers().get(guild.getIdLong()).destroy();
    }

}
