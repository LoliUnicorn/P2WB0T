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

package pl.kamil0024.music.audiomanager.spotify;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kamil0024.core.Ustawienia;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    // based on code dunctebot
    private static final Pattern SPOTIFY_TRACK_REGEX = Pattern.compile("^(?:spotify:(track:)|(?:http://|https://)[a-z]+\\.spotify\\.com/track/([a-zA-z0-9]+))(?:.*)$");
    private static final Pattern SPOTIFY_ALBUM_REGEX = Pattern.compile("^(?:spotify:(track:)|(?:http://|https://)[a-z]+\\.spotify\\.com/album/([a-zA-z0-9]+))(?:.*)$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX = Pattern.compile("^(?:spotify:(track:)|(?:http://|https://)[a-z]+\\.spotify\\.com/)playlist/([a-zA-z0-9]+)(?:.*)$");
    private static final Pattern SPOTIFY_PLAYLIST_REGEX_USER = Pattern.compile("^(?:spotify:(track:)|(?:http://|https://)[a-z]+\\.spotify\\.com/)user/(.*)/playlist/([a-zA-z0-9]+)(?:.*)$");
    private static final Pattern SPOTIFY_SECOND_PLAYLIST_REGEX = Pattern.compile("^(?:spotify:user:)(?:.*)(?::playlist:)(.*)$");

    private final Logger logger = LoggerFactory.getLogger(SpotifyAudioSourceManager.class);

    private String accessToken = null;
    private long expire = 0;
    private final SpotifyApi api;
    private final YoutubeAudioSourceManager youtubeManager;
    private final List<Function<AudioReference, AudioItem>> loaders;

    public SpotifyAudioSourceManager(YoutubeAudioSourceManager youtubeManager) {
        this.api = SpotifyApi.builder().setClientId(Ustawienia.instance.spotify.id).setClientSecret(Ustawienia.instance.spotify.secret).build();
        this.youtubeManager = youtubeManager;
        this.loaders = Arrays.asList(this::getSpotifyTrack, this::getSpotifyAlbum, this::getSpotifyPlaylist);
    }

    private void refreshToken() {
        if (System.currentTimeMillis() > expire)
            try {
                ClientCredentialsRequest request = api.clientCredentials().build();
                Future<ClientCredentials> clientCredentialsFuture = request.executeAsync();
                ClientCredentials clientCredentials = clientCredentialsFuture.get();
                expire = clientCredentials.getExpiresIn() * 1000;
                api.setAccessToken(accessToken = clientCredentials.getAccessToken());
                logger.debug("Refreshed access token: {}, expires in {} seconds",
                        clientCredentials.getAccessToken(), clientCredentials.getExpiresIn());
            } catch (Exception e) {
                accessToken = null;
                logger.error("Error refreshing Spotify access token!", e);
            }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        refreshToken();
        if (accessToken == null)
            return null;

        AudioItem item;
        for (Function<AudioReference, AudioItem> loader : loaders) {
            if ((item = loader.apply(reference)) != null)
                return item;
        }
        return null;
    }

    private AudioItem getSpotifyAlbum(AudioReference reference) {
        Matcher res = SPOTIFY_ALBUM_REGEX.matcher(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        try {
            List<AudioTrack> playlist = new ArrayList<>();

            Future<Album> albumFuture = api.getAlbum(res.group(res.groupCount())).build().executeAsync();
            Album album = albumFuture.get();

            for (TrackSimplified t : album.getTracks().getItems()) {
                // todo caching!
                //AudioItem item = youtubeManager.loadItem(null, new AudioReference("ytsearch:" + album.getArtists()[0].getName() + " " + t.getName(), null));
                // if (item instanceof AudioPlaylist)
                //     playlist.add(((AudioPlaylist) item).getTracks().get(0));
                AudioTrackInfo info = new AudioTrackInfo(t.getName(), album.getArtists()[0].getName(), t.getDurationMs(),
                        "ytsearch:" + album.getArtists()[0].getName() + " " + t.getName(), false, null);
                playlist.add(new SpotifyAudioTrack(info, youtubeManager));
            }

            return new BasicAudioPlaylist(album.getName(), playlist, playlist.get(0), false);
        } catch (Exception e) {
            logger.error("oops!", e);
            throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
        }
    }

    @SuppressWarnings("deprecated")
    private AudioItem getSpotifyPlaylist(AudioReference reference) {
        Matcher res = getSpotifyPlaylistFromString(reference.identifier);

        if (!res.matches()) {
            return null;
        }

        String playListId = res.group(res.groupCount());
        String userId = res.group(res.groupCount() - 1);

        try {
            final List<AudioTrack> finalPlaylist = new ArrayList<>();

            final Future<Playlist> playlistFuture;

            playlistFuture = api.getPlaylist(playListId).build().executeAsync();

            final Playlist spotifyPlaylist = playlistFuture.get();

            for (PlaylistTrack playlistTrack : spotifyPlaylist.getTracks().getItems()) {
                AudioTrackInfo info = new AudioTrackInfo(playlistTrack.getTrack().getName(), playlistTrack.getTrack().getArtists()[0].getName(), playlistTrack.getTrack().getDurationMs(),
                        "ytsearch:" + playlistTrack.getTrack().getArtists()[0].getName() + " " + playlistTrack.getTrack().getName(), false, null);
                finalPlaylist.add(new SpotifyAudioTrack(info, youtubeManager));
            }
            if (finalPlaylist.isEmpty())
                return null;

            return new BasicAudioPlaylist(spotifyPlaylist.getName(), finalPlaylist, finalPlaylist.get(0), false);
        } catch (IllegalArgumentException e) {
            logger.error("oops!", e);
            throw new FriendlyException("This playlist could not be loaded, make sure that it's public", FriendlyException.Severity.COMMON, e);
        } catch (Exception e) {
            logger.error("oops!", e);
            throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
        }
    }

    private Matcher getSpotifyPlaylistFromString(String input) {
        Matcher match = SPOTIFY_PLAYLIST_REGEX.matcher(input);
        if (match.matches())
            return match;

        Matcher withUser = SPOTIFY_PLAYLIST_REGEX_USER.matcher(input);
        if (withUser.matches())
            return withUser;

        return SPOTIFY_SECOND_PLAYLIST_REGEX.matcher(input);
    }

    private AudioItem getSpotifyTrack(AudioReference reference) {
        Matcher res = SPOTIFY_TRACK_REGEX.matcher(reference.identifier);
        if (!res.matches()) {
            return null;
        }

        try {
            Future<Track> trackFuture = api.getTrack(res.group(res.groupCount())).build().executeAsync();
            Track track = trackFuture.get();

            AudioTrackInfo info = new AudioTrackInfo(track.getName(), track.getArtists()[0].getName(), track.getDurationMs(),
                    "ytsearch:" + track.getArtists()[0].getName() + " " + track.getName(), false, null);

            return new SpotifyAudioTrack(info, youtubeManager);
        } catch (Exception e) {
            logger.error("oops!", e);
            throw new FriendlyException(e.getMessage(), FriendlyException.Severity.FAULT, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        throw new UnsupportedOperationException("Not supported by this audio source manager");
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        throw new UnsupportedOperationException("Not supported by this audio source manager");
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        //
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        //
    }
}
