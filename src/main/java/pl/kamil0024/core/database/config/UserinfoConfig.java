package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("userinfo")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class UserinfoConfig {
    public UserinfoConfig() {}



}
