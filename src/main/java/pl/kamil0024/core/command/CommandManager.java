package pl.kamil0024.core.command;

import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.core.logger.Log;

import java.util.*;

public class CommandManager extends ListenerAdapter {

    @Getter public Set<Command> registered;
    @Getter public Map<String, Command> commands;
    @Getter public Map<String, Command> aliases;

    public CommandManager() {
        this.commands = new HashMap<>();
        this.registered = new HashSet<>();
        this.aliases = new HashMap<>();
    }

    public void registerCommand(Command command) {
        if (command == null) return;
        if (commands.containsKey(command.toString())) throw new IllegalArgumentException(String.format("Komenda o nazwie %s jest juz zarejestrowana (%s)", command.toString(), command.getClass().getName()));
        if (command.getName() == null || command.getName().isEmpty()) throw new NullPointerException("Nazwa jest pusta! " + command.getClass().getName());
        registered.add(command);
        commands.put(command.toString(), command);
        Log.debug("Register command %s", command.getName());
        registerAliases(command);
    }

    public void registerAliases(Command command) {
        if (command.getAliases().isEmpty()) return;
        command.getAliases().forEach(alias -> {
            getAliases().put(alias, command);
            aliases.put(alias, command);
        });
    }

    public void unregisterCommands(List<Command> cmds) {
        for (Command command : cmds) {
            commands.values().removeIf(cmd -> command == cmd);
            registered.removeIf(cmd -> command == cmd);

            commands.values().removeIf(cmd -> cmd.toString().equals(command.toString()));
            registered.removeIf(cmd -> cmd.toString().equals(command.toString()));
        }
    }

    public void unregisterAll() {
        registered = new HashSet<>();
        commands = new HashMap<>();
    }

}
