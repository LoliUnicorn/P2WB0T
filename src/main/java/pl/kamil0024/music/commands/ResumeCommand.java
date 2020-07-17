package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
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
        this.musicModule = musicModule;
    }

    @SuppressWarnings("UnusedAssignment")
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

        GuildMusicManager audio = musicModule.getGuildAudioPlayer(context.getGuild());
        if (audio.getPlayer().getPlayingTrack() == null) {
            context.send("Nic nie gram!").queue();
            return false;
        }

        String tak = " piosenkę";

        if (audio.getPlayer().isPaused()) {
            tak = "Wznawiam";
            audio.getPlayer().setPaused(false);
        } else {
            tak = "Zatrzymuje";
            audio.getPlayer().setPaused(true);
        }
        context.send(tak).queue();
        return true;
    }

}
