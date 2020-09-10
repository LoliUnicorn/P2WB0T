/*
 * Copyright (C) 2019-2020 FratikB0T Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.kamil0024.core;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class Ustawienia {
    @SuppressWarnings("squid:S1444")
    public static Ustawienia instance;
    public static String def = "12345";

    public String prefix = "k!";
    public String language = "pl";
    public String muteRole = def;
    public List<String> devs = Arrays.asList("343467373417857025");
    public List<String> disabledCommand = Arrays.asList("647904617811804202");

    public Roles roles = new Roles();
    public Bot bot = new Bot();
    public Games games = new Games();
    public Emote emote = new Emote();
    public PostgresSettings postgres = new PostgresSettings();
    public WebhookConfig webhook = new WebhookConfig();
    public Channel channel = new Channel();
    public Inne inne = new Inne();
    public Api api = new Api();
    public Rangi rangi = new Rangi();
    public YouTrack yt = new YouTrack();
    public SpotifyCredentials spotify = new SpotifyCredentials();

    public static class Roles {
        public String helperRole = def;
        public String moderatorRole = def;
        public String adminRole = def;
        public String chatMod = def;
    }

    public static class Bot {
        public String botId = def;
        public String guildId = def;
    }

    public static class Games {
        public List<String> games = Arrays.asList("v{VERSION}", "{USERS:ALL} użytkowników", "{PREFIX}help");
    }

    public static class Emote {
        public String green = def;
        public String red = def;
        public String load = def;
    }

    public static class PostgresSettings {
        @SerializedName("jdbc-url")
        public String jdbcUrl = "jdbc:postgresql://localhost/test";
        public String user = "postgres";
        public String password = def;
    }

    public static class WebhookConfig {
        public String error = "https://discordapp.com/api/webhooks/";
        public String cmd = "https://discordapp.com/api/webhooks/";
        public String status = "https://discordapp.com/api/webhooks/";
        public String debug = "https://discordapp.com/api/webhooks/";
        public String cases = "https://discordapp.com/api/webhooks/";
    }

    public static class Channel {
        public String modlog = def;
        public String nieobecnosci = def;
        public String status = def;
        public String liczek = def;
        public String wiadomosci = def;
        public String moddc = def;
        public String loginieobecnosci = def;
    }

    public static class Inne {
        public String kategoriaArchiwum = def;
    }

    public static class Api {
        public Integer port;
        public List<String> tokens = Arrays.asList("343467373417857025");
    }

    public static class Rangi {
        public String gracz = def;
        public String vip = def;
        public String vipplus = def;
        public String mvp = def;
        public String mvpplus = def;
        public String mvpplusplus = def;
        public String sponsor = def;
        public String miniyt = def;
        public String yt = def;
        public String pomocnik = def;
        public String buildteam = def;
        public String stazysta = def;
        public String moderator = def;
        public String administrator = def;
        public String korona = def;
    }

    public static class YouTrack {
        public String url = def;
        public String hub = def;
        public String ytId = def;
        public String clientSecret = def;
        public String clientScope = def;
    }

    public static class SpotifyCredentials {
        public String id;
        public String secret;
    }


}
