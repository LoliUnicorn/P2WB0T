package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class LoopCommand extends Command {

    private MusicModule musicModule;

    public LoopCommand(MusicModule musicModule) {
        name = "loop";
        permLevel = PermLevel.HELPER;
        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!PlayCommand.isVoice(context.getGuild().getSelfMember())) {
            context.sendTranslate("leave.nochannel").queue();
            return false;
        }

        if (!PlayCommand.isSameChannel(context.getGuild().getSelfMember(), context.getMember())) {
            context.sendTranslate("leave.samechannel").queue();
            return false;
        }

        GuildMusicManager musicManager = musicModule.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            context.sendTranslate("resume.noplay").queue();
            return false;
        }

        if (musicManager.getScheduler().getLoop()) {
            context.sendTranslate("loop.off").queue();
            musicManager.getScheduler().setLoop(false);
            return true;
        }

        context.sendTranslate("loop.on").queue();
        musicManager.getScheduler().setLoop(true);
        return true;
    }

}
