package pl.kamil0024.musicbot.api.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.music.managers.MusicManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class LeaveVcListener extends ListenerAdapter {

    private final MusicManager musicManager;

    public LeaveVcListener(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    @Override
    public void onGuildVoiceMove(@Nonnull GuildVoiceMoveEvent event) {
        if (!event.getEntity().getId().equals(event.getGuild().getSelfMember().getId())) {
            return;
        }
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
        if (event.getEntity().getId().equals(event.getGuild().getSelfMember().getId())) {
            musicManager.getMusicManagers().get(guild.getIdLong()).destroy();
            return;
        }

        GuildVoiceState state = event.getGuild().getSelfMember().getVoiceState();
        if (state == null || state.getChannel() == null || !state.getChannel().getId().equals(event.getChannelLeft().getId())) {
            return;
        }

        List<Member> members = event.getChannelLeft().getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toList());
        int size = members.size();

        if (size < 4) {
            musicManager.getMusicManagers().get(guild.getIdLong()).destroy();
        }

    }

}
