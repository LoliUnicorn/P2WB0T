package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
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

}
