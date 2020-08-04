package pl.kamil0024.core.database.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscordInviteConfig {
    public DiscordInviteConfig() {}

    public DiscordInviteConfig(String nick) {
        this.nick = nick;
    }

    private String nick = "";
    private String kod = null;
    private String ranga = null;

}
