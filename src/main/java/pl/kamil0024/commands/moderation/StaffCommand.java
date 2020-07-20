package pl.kamil0024.commands.moderation;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.ServerListPing17;
import pl.kamil0024.core.util.UsageException;

import java.net.InetSocketAddress;

public class StaffCommand extends Command {

    public StaffCommand() {
        name = "staff";
        permLevel = PermLevel.HELPER;
    }

    @Override
    public boolean execute(CommandContext context) {
        String typ = context.getArgs().get(0);
        if (typ == null) throw new UsageException();
        typ = typ.toLowerCase();

        if (typ.equals("list") || typ.equals("lista")) {
            try {
                ServerListPing17.StatusResponse resp = new ServerListPing17(new InetSocketAddress("137.74.4.174", 25571)).fetchData();
                for (ServerListPing17.Player player : resp.getPlayers().getSample()) {
                    Log.debug(player.getName());
                }
            } catch (Exception e) {
                Log.newError(e);
                context.send("Wystąpił błąd! Zobacz/poproś dewelopera o zobaczenia logów").queue();
                return false;
            }
            return true;
        }

        throw new UsageException();
    }

}
