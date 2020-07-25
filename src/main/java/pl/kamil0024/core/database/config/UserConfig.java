package pl.kamil0024.core.database.config;

import lombok.Data;

@Data
public class UserConfig {

    public UserConfig(String id) {
        this.id = id;
    }

    private String id = "";

    private String mcNick = null;
    private String fullname = "/";

}
