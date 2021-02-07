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

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.Message;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Data
public class FakeMessage {

    private final String id;
    private final String author;
    private final String content;
    private final String channel;
    private final OffsetDateTime createdAt;

    public static FakeMessage convert(Message msg) {
        return new FakeMessage(msg.getId(),
                msg.getAuthor().getId(),
                msg.getContentRaw(),
                msg.getTextChannel().getId(), msg.getTimeCreated());
    }

}
