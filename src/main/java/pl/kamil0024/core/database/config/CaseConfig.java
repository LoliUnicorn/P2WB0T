package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.kamil0024.core.util.kary.Kara;

@Table("cases")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class CaseConfig {

    public CaseConfig() {}

    public CaseConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    private Kara kara = null;

}