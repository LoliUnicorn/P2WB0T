package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class ResumeCommand extends Command {

    private MusicModule musicModule;

    public ResumeCommand(MusicModule musicModule) {
        name = "resume";
        aliases.add("stop");

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

        GuildMusicManager audio = musicModule.getGuildAudioPlayer(context.getGuild());
        if (audio.getPlayer().getPlayingTrack() == null) {
            context.sendTranslate("resume.noplay").queue();
            return false;
        }

        String tak = " piosenkę";

        if (audio.getPlayer().isPaused()) {
            tak = context.getTranslate("resume.resumed");
            audio.getPlayer().setPaused(false);
        } else {
            tak = context.getTranslate("resume.stoped");
            audio.getPlayer().setPaused(true);
        }
        context.send(tak).queue();
        return true;
    }

}
