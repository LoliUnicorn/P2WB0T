package pl.kamil0024.music.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;

import java.util.Objects;

public class PlayCommand extends Command {

    private MusicModule musicModule;

    public PlayCommand(MusicModule musicModule) {
        name = "play";
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MUSIC;

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        String url = context.getArgs().get(0);
        if (url == null) throw new UsageException();
        
        if (!isVoice(context.getMember())) {
            context.sendTranslate("play.nochannel").queue();
            return false;
        }

        if (isVoice(context.getGuild().getSelfMember()) && !isSameChannel(context.getGuild().getSelfMember(), context.getMember())) {
            if (!getVc(context.getGuild().getSelfMember()).getId().equals(getVc(context.getMember()).getId())) {
                context.sendTranslate("leave.samechannel").queue();
                return false;
            }
        }
        
        if (!hasPermission(context.getGuild().getSelfMember(), getVc(context.getMember()))) {
            context.sendTranslate("play.noperms").queue();
            return false;
        }

        return musicModule.loadAndPlay(context.getChannel(), url, getVc(context.getMember()));
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

    public static boolean isSameChannel(Member bot, Member mem) {
        if (!PlayCommand.isVoice(bot)) {
            return false;
        }
        return PlayCommand.isVoice(mem) && getVc(mem).getId().equals(getVc(bot).getId());
    }

}
