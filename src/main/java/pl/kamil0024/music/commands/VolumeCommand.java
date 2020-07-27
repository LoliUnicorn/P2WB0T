package pl.kamil0024.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;

@SuppressWarnings("DuplicatedCode")
public class VolumeCommand extends Command {

    private MusicModule musicModule;

    public VolumeCommand(MusicModule musicModule) {
        name = "volume";
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
        AudioPlayer audio = musicModule.getGuildAudioPlayer(context.getGuild()).getPlayer();
        Integer arg = context.getParsed().getNumber(context.getArgs().get(0));
        if (arg == null) {
            context.sendTranslate("volume.volume", audio.getVolume()).queue();
            return true;
        }

        if (arg < 1 || arg > 100) {
            context.sendTranslate("Mvolume.badnumber").queue();
            return false;
        }

        audio.setVolume(arg);
        context.sendTranslate("volume.succes", arg).queue();
        return true;
    }

}
