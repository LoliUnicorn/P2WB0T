package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Table("voicestate")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class VoiceStateConfig {
    public VoiceStateConfig() {}

    public VoiceStateConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id;

    private String voiceChannel = null;
    ArrayList<String> queue = null;
    String aktualnaPiosenka = null;

}
