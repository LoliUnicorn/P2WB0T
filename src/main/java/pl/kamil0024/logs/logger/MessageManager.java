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

package pl.kamil0024.logs.logger;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MessageManager extends ListenerAdapter {

    @Getter public Map<String, FakeMessage> map;

    public MessageManager() {
        map = new HashMap<>();
    }

    public void add(Message message) {
        map.put(message.getId(), FakeMessage.convert(message));
    }

    public void edit(Message message) {
        map.remove(message.getId());
        add(message);
    }

    public boolean exists(String id) {
        return map.get(id) != null;
    }

    public FakeMessage get(String id) {
        if (!exists(id)) return null;
        return map.get(id);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake() || event.getMessage().isWebhookMessage() ||
                event.getMessage().getContentRaw().isEmpty()) return;
        add(event.getMessage());
    }

}

