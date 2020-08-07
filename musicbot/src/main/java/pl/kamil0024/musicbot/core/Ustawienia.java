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
