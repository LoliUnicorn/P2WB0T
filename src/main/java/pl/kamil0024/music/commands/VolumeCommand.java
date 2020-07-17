package pl.kamil0024.music.commands;

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
            context.send("Nie jestem na żadnym kanale głosowym!").queue();
            return false;
        }

        if (!PlayCommand.isSameChannel(context.getGuild().getSelfMember(), context.getMember())) {
            context.send("Musisz być połączony z tym samym kanałem głosowym co bot!").queue();
            return false;
        }

        Integer arg = context.getParsed().getNumber(context.getArgs().get(0));
        if (arg == null) throw new UsageException();

        if (arg < 1 || arg > 100) {
            context.send("Musisz wybrać liczbę, która jest pomiędzy 1 a 100").queue();
            return false;
        }
        musicModule.getGuildAudioPlayer(context.getGuild()).getPlayer().setVolume(arg);
        context.send("Pomyślnie zmieniono głośność na " + arg + "%").queue();
        return true;
    }

}
