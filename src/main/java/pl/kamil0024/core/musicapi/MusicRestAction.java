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

package pl.kamil0024.core.musicapi;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.music.commands.QueueCommand;

import java.io.IOException;

@SuppressWarnings("unused")
public interface MusicRestAction {

    MusicResponse testConnection();

    MusicResponse connect(String channelId) throws Exception;
    default MusicResponse connect(VoiceChannel vc) throws Exception {
        return connect(vc.getId());
    }

    MusicResponse disconnect() throws Exception;

    VoiceChannel getVoiceChannel();

    MusicResponse shutdown() throws IOException;

    MusicResponse play(String link) throws IOException;
    default MusicResponse play(AudioTrack track) throws IOException {
        return play(track.getIdentifier());
    }

    MusicResponse skip() throws IOException;
    MusicResponse volume(Integer procent) throws IOException;

    String clientid();

    MusicResponse getQueue() throws IOException;
    MusicResponse getPlayingTrack() throws IOException;

}
