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

package pl.kamil0024.core.console.impl;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.console.Console;
import pl.kamil0024.core.console.ConsoleCommandContext;
import pl.kamil0024.core.console.commands.PingCommand;
import pl.kamil0024.core.console.commands.RebootCommand;
import pl.kamil0024.core.logger.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleImpl implements Console {

    private BufferedReader buffered = null;
    private boolean stop = false;

    @Override
    public void start() {
        buffered = new BufferedReader(new InputStreamReader(System.in));
        while (!stop) {

            System.out.print("> ");
            String input;
            try {
                input = buffered.readLine().toLowerCase();
                ConsoleCommandContext context = new ConsoleCommandContext(input);

                if (input.equals("ping")) new PingCommand().execute(context);
                if (input.equals("reboot")) new RebootCommand().execute(context);

                else Log.error("Nie ma takiej komendy!");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void stop() {
        this.stop = true;
    }

}
