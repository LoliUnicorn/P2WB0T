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

package pl.kamil0024.musicbot.api.handlers;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Data;

public class QueueHandler {

    @Data
    public static class Track {
        
        private final String identifier;
        private final String author;
        private final String title;
        private final boolean stream;
        private final long length;
        private final long position;

        public Track(AudioTrack audioTrack) {
            this.identifier = audioTrack.getIdentifier();
            this.author = audioTrack.getInfo().author;
            this.title = audioTrack.getInfo().title;
            this.stream = audioTrack.getInfo().isStream;
            this.length = audioTrack.getInfo().length;
            this.position = audioTrack.getPosition();
        }
        
    }

}
