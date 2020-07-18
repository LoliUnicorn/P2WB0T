package pl.kamil0024.commands.dews;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.stats.StatsModule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class StopCommand extends Command {

    private ModulManager modulManager;
    private StatsModule statsModule;
    private EventWaiter eventWaiter;

    public StopCommand(ModulManager modulManager, StatsModule statsModule, EventWaiter eventWaiter) {
        name = "stop";
        permLevel = PermLevel.DEVELOPER;

        this.statsModule = statsModule;
        this.modulManager = modulManager;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        context.send("Wyłączam...").complete();

        context.getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
        context.getShardManager().setActivity(Activity.playing("Wyłącznie bota w toku..."));

        modulManager.disableAll();
        statsModule.getStatsCache().databaseSave();

        context.send("Zrobić builda? (y/n)").queue();

        AtomicBoolean build = new AtomicBoolean(false);
        AtomicBoolean restart = new AtomicBoolean(false);

        eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                (event) -> event.getAuthor().getId().equals(context.getUser().getId()) && event.getChannel().getId().equals(context.getChannel().getId()),
                (event) -> {
                    if (event.getMessage().getContentRaw().toLowerCase().equals("y")) build.set(true);
                    context.send("Uruchomić ponownie bota? (y/n)").queue();

                    eventWaiter.waitForEvent(GuildMessageReceivedEvent.class,
                            (event1) -> event1.getAuthor().getId().equals(context.getUser().getId()) && event1.getChannel().getId().equals(context.getChannel().getId()),
                            (event1) -> {
                                if (event1.getMessage().getContentRaw().toLowerCase().equals("y")) restart.set(true);

                                if (build.get()) {
                                    context.send("Robię builda...").complete();
                                    ShellCommand.shell("cd /home/debian/P2WB0T && ./start.sh");
                                }
                                if (restart.get()) {
                                    context.send("Restartuje bota...").complete();
                                    ShellCommand.shell("cd /home/debian/core && ./start.sh");
                                }
                                context.getShardManager().shutdown();
                                System.exit(0);
                            }, 1, TimeUnit.MINUTES, () -> {}
                    );

                }, 1, TimeUnit.MINUTES, () -> {}
        );
        return true;
    }

}
