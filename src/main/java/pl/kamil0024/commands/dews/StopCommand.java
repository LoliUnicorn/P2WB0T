package pl.kamil0024.commands.dews;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import pl.kamil0024.core.B0T;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.stats.StatsModule;

public class StopCommand extends Command {

    private ModulManager modulManager;
    private StatsModule statsModule;

    public StopCommand(ModulManager modulManager, StatsModule statsModule) {
        name = "stop";
        permLevel = PermLevel.DEVELOPER;

        this.statsModule = statsModule;
        this.modulManager = modulManager;
    }

    @Override
    public boolean execute(CommandContext context) {
        context.send("Wyłączam...").complete();

        context.getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
        context.getShardManager().setActivity(Activity.playing("Wyłącznie bota w toku..."));

        modulManager.disableAll();
        statsModule.getStatsCache().databaseSave();

        context.getShardManager().shutdown();

        System.exit(0);
        return true;
    }

}
