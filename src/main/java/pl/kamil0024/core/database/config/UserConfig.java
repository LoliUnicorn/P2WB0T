package pl.kamil0024.core.database.config;

import lombok.Getter;
import lombok.Setter;

public class UserConfig {

    public UserConfig(String id) {
        this.id = id;
    }

    @Getter @Setter private String id = "";

    @Getter @Setter private String mcNick = null;
    @Getter @Setter private String fullname = "/";

}
