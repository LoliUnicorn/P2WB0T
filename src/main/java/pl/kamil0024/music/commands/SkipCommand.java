package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class SkipCommand extends Command {

    private MusicModule musicModule;

    public SkipCommand(MusicModule musicModule) {
        name = "skip";
        permLevel = PermLevel.HELPER;

        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!PlayCommand.isVoice(context.getGuild().getSelfMember())) {
            context.send("Nie jestem na żadnym kanale głosowym!").queue();
            return false;
        }

        if (!PlayCommand.isSameChannel(context.getGuild().getSelfMember(), context.getMember())) {
            context.send("Musisz być połączony z tym samym kanałem głosowym co bot!").queue();
            return false;
        }

        GuildMusicManager musicManager = musicModule.getGuildAudioPlayer(context.getGuild());

        if (musicManager.getPlayer().getPlayingTrack() == null) {
            context.send("Nic nie gram!").queue();
            return false;
        }
        context.send("Puszczam następną piosenkę").queue();
        musicManager.getScheduler().nextTrack();
        return true;
    }

}
