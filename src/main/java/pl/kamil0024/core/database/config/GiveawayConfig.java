package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Table("konkurs")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class GiveawayConfig {

    public GiveawayConfig() { }

    public GiveawayConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    private String organizator;
    private String nagroda;
    private String messageId;
    private String kanalId;

    private long start;
    private long end;

    private int wygranychOsob = 1;

    private boolean aktywna = true;

    private List<String> winners = new ArrayList<>();

}
