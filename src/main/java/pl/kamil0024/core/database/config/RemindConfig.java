package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("remind")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class RemindConfig {
    public RemindConfig() {}

    public RemindConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    private String userId = null;

    String tresc = null;
    String msg = null;
    long czas = 0;

}
