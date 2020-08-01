package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class LeaveCommand extends Command {

    private MusicModule musicModule;

    public LeaveCommand(MusicModule musicModule) {
        name = "leave";
        aliases.add("opusc");
        category = CommandCategory.MUSIC;
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
        musicManager.getScheduler().destroy();
        context.sendTranslate("leave.succes", "\uD83D\uDC4B").queue();
        return true;
    }

}
