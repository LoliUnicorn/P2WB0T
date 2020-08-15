/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.core.command;

import com.google.gson.Gson;
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
        Log.debug("Rejestruje komende %s (%s)", command.getName());
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
