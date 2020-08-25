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

package pl.kamil0024.musicbot.core;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class Ustawienia {
    @SuppressWarnings("squid:S1444")
    public static Ustawienia instance;
    public static String def = "12345";

    public Bot bot = new Bot();
    public PostgresSettings postgres = new PostgresSettings();
    public WebhookConfig webhook = new WebhookConfig();
    public Api api = new Api();

    public static class Bot {
        public String botId = def;
        public String guildId = def;
    }

    public static class PostgresSettings {
        @SerializedName("jdbc-url")
        public String jdbcUrl = "jdbc:postgresql://localhost/musicbot";
        public String user = "postgres";
        public String password = def;
    }

    public static class WebhookConfig {
        public String error = "https://discordapp.com/api/webhooks/";
        public String cmd = "https://discordapp.com/api/webhooks/";
        public String status = "https://discordapp.com/api/webhooks/";
        public String debug = "https://discordapp.com/api/webhooks/";
    }

    public static class Channel {
        public String modlog = def;
        public String nieobecnosci = def;
        public String status = def;
        public String liczek = def;
        public String wiadomosci = def;
        public String moddc = def;
    }

    public static class Inne {
        public String kategoriaArchiwum = def;
    }

    public static class Api {
        public int port = 0;
        public int mainPort = 0;
        public List<String> tokens = Arrays.asList("12345");
    }

}
