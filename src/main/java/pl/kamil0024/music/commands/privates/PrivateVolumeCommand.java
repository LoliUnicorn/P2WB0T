package pl.kamil0024.music.commands.privates;

import net.dv8tion.jda.api.entities.Member;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.commands.PlayCommand;

@SuppressWarnings("DuplicatedCode")
public class PrivateVolumeCommand extends Command {

    private MusicAPI musicAPI;

    public PrivateVolumeCommand(MusicAPI musicAPI) {
        name = "pvolume";
        aliases.add("privatevolume");
        category = CommandCategory.PRIVATE_CHANNEL;
        this.musicAPI = musicAPI;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!PrivatePlayCommand.check(context)) return false;

        Integer liczba = context.getParsed().getNumber(context.getArgs().get(0));
        if (liczba == null) throw new UsageException();
        if (liczba <= 0 || liczba > 100) {
            context.send("Liczba musi być pomiędzy **1** a **100**").queue();
            return false;
        }

        int wolnyBot = 0;
        MusicRestAction restAction = null;

        for (Member member : PlayCommand.getVc(context.getMember()).getMembers()) {
            if (member.getUser().isBot()) {
                Integer agent = musicAPI.getPortByClient(member.getId());
                if (agent != null) {
                    wolnyBot = agent;
                    restAction = musicAPI.getAction(agent);
                }

            }
        }

        if (wolnyBot == 0) {
            context.send("Na Twoim kanale nie ma żadnego bota").queue();
            return false;
        }

        try {
            MusicResponse skip = restAction.volume(liczba);
            if (skip.isError()) {
                context.send("Wystąpił błąd: " + skip.getError().getDescription()).queue();
                return false;
            }
            context.send("Pomyślnie zmieniono głośność na " + liczba + " procent").queue();
            return true;
        } catch (Exception e) {
            context.send("Wystąpił błąd: " + e.getLocalizedMessage()).queue();
            return false;
        }
    }

}
