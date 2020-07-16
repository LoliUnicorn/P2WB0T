package pl.kamil0024.music.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.internal.entities.VoiceChannelImpl;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.music.MusicModule;

import java.util.Objects;

public class PlayCommand extends Command {

    private MusicModule musicModule;

    public PlayCommand(MusicModule musicModule) {
        name = "play";

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        String url = context.getArgsToString(0);
        
        if (!isVoice(context.getMember())) {
            context.send("Musisz być połączony z kanałem głosowym!").queue();
            return false;
        }
        
        if (!hasPermission(context.getGuild().getSelfMember(), getVc(context.getMember()))) {
            context.send("Nie mam wystarczających uprawnień do dołączenia na serwer!").queue();
            return false;
        }

        return musicModule.loadAndPlay(context.getChannel(), url);
    }
    
    public static boolean isVoice(Member member) {
        GuildVoiceState gvc = member.getVoiceState();
        if (gvc == null) return false;
        return gvc.getChannel() != null;
    }
    
    @NotNull
    public static VoiceChannel getVc(Member mem) {
        return Objects.requireNonNull(Objects.requireNonNull(mem.getVoiceState()).getChannel());
    }
    
    public static boolean hasPermission(Member mem, VoiceChannel vc) {
        return mem.hasPermission(vc, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
    }

}
