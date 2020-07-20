package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.kamil0024.core.util.Nick;

import java.util.ArrayList;

@Table("multi")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class MultiConfig {
    public MultiConfig() {}

    public MultiConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id;

    ArrayList<Nick> nicki = new ArrayList<>();

}
