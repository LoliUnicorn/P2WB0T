package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class SkipCommand extends Command {

    private MusicModule musicModule;

    public SkipCommand(MusicModule musicModule) {
        name = "skip";
        permLevel = PermLevel.HELPER;
        category = CommandCategory.MUSIC;

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
        context.sendTranslate("skip.next").queue();
        musicManager.getScheduler().nextTrack();
        return true;
    }

}
