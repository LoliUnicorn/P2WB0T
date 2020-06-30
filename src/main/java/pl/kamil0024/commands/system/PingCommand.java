package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;

import java.time.temporal.ChronoUnit;

public class PingCommand extends Command {

    public PingCommand() {
        name = "ping";
    }

    @Override
    public boolean execute(@NotNull CommandContext context) {
        Message msg = context.send(context.getTranslate("ping.ping")).complete();
        long ping = context.getEvent().getMessage().getTimeCreated().until(msg.getTimeCreated(), ChronoUnit.MILLIS);
        msg.editMessage(context.getTranslate("ping.pong", ping, context.getEvent().getJDA().getGatewayPing())).queue();
        return true;
    }

}

