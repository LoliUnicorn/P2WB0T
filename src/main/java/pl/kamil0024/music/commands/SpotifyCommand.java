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

package pl.kamil0024.music.commands;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;
import pl.kamil0024.music.audiomanager.spotify.SpotifyAudioSourceManager;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;
import pl.kamil0024.music.commands.PlayCommand.*;

@SuppressWarnings("DuplicatedCode")
public class SpotifyCommand extends Command {

    private MusicModule musicModule;
    private SpotifyAudioSourceManager spotify;

    public SpotifyCommand(MusicModule musicModule, SpotifyAudioSourceManager spotify) {
        name = "spotify";
        aliases.add("sp");
        this.musicModule = musicModule;
        this.spotify = spotify;
    }
    
    @Override
    public boolean execute(CommandContext context) {
        if (!PlayCommand.isVoice(context.getMember())) {
            context.sendTranslate("play.nochannel").queue();
            return false;
        }

        if (PlayCommand.isVoice(context.getGuild().getSelfMember()) && !PlayCommand.isSameChannel(context.getGuild().getSelfMember(), context.getMember())) {
            if (!PlayCommand.getVc(context.getGuild().getSelfMember()).getId().equals(PlayCommand.getVc(context.getMember()).getId())) {
                context.sendTranslate("leave.samechannel").queue();
                return false;
            }
        }

        if (!PlayCommand.hasPermission(context.getGuild().getSelfMember(), PlayCommand.getVc(context.getMember()))) {
            context.sendTranslate("play.noperms").queue();
            return false;
        }
        GuildMusicManager manager = musicModule.getGuildAudioPlayer(context.getGuild());
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();
        AudioItem music = spotify.loadItem(musicModule.defaultAudioPlayerManager, new AudioReference(arg, "?"));
        if (music == null) {
            context.send("Nie znaleziono takiej piosenki!").queue();
            return false;
        }
        if (music instanceof AudioPlaylist) {
            musicModule.play(context.getGuild(), manager, (AudioTrack) music, PlayCommand.getVc(context.getMember()));
        }
        context.send("!(music instanceof AudioPlaylist)").queue();
        return false;
    }
    
}
