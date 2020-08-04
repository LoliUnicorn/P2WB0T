package pl.kamil0024.core.database.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class UserinfoConfig {
    public UserinfoConfig() {}

    public UserinfoConfig(String id) {
        this.id = id;
    }

    @Getter @Setter private String id = "";

    @Getter @Setter private String mcNick = null;
    @Getter @Setter private String fullname = "/";

    public String getWhateverName() {
        return getMcNick() == null ? getFullname() : getMcNick();
    }

}
