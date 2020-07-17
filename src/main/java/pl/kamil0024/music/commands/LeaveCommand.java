package pl.kamil0024.music.commands;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

@SuppressWarnings("DuplicatedCode")
public class LeaveCommand extends Command {

    private MusicModule musicModule;

    public LeaveCommand(MusicModule musicModule) {
        name = "leave";
        aliases.add("opusc");

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
        musicManager.getScheduler().destroy();
        context.send("Wychodze z kanału \uD83D\uDC4B");
        return true;
    }

}
