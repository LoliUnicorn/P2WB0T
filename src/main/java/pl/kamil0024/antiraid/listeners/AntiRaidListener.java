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

package pl.kamil0024.antiraid.listeners;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.antiraid.managers.AntiRaidManager;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UserUtil;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class AntiRaidListener extends ListenerAdapter {

    private final AntiRaidManager antiRaidManager;
    
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
//        if (event.getAuthor().isBot() || UserUtil.getPermLevel(event.getMember()).getNumer() > PermLevel.MEMBER.getNumer())
//            return;
        Log.debug("wykonuje event");
        if (!event.getAuthor().getId().equals("343467373417857025")) return;

        Log.debug("zapisuje wiadomosc");
        antiRaidManager.saveMessage(event.getAuthor().getId(), event.getMessage());
    }

}
