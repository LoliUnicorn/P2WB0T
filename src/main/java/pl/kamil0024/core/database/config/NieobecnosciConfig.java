package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;

import java.util.ArrayList;

@Table("nieobecnosci")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class NieobecnosciConfig {

    public NieobecnosciConfig() { }

    public NieobecnosciConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    private ArrayList<Nieobecnosc> nieobecnosc = new ArrayList<>();

}
