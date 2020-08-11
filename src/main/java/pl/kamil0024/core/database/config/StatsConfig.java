package pl.kamil0024.core.database.config;

import gg.amy.pgorm.annotations.GIndex;
import gg.amy.pgorm.annotations.PrimaryKey;
import gg.amy.pgorm.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONPropertyIgnore;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.bdate.util.Nullable;
import pl.kamil0024.stats.entities.Statystyka;

import java.util.ArrayList;

@Table("stats")
@GIndex({"id"})
@Data
@AllArgsConstructor
public class StatsConfig {
    public StatsConfig() {}

    public StatsConfig(String id) {
        this.id = id;
    }

    @PrimaryKey
    private String id = "";

    public ArrayList<Statystyka> stats = new ArrayList<>();

    @Nullable
    public static Statystyka getStatsFromDate(ArrayList<Statystyka> stats, long date) {
        int bDate = new BDate(date).getDateTime().getDayOfYear();
        for (Statystyka stat : stats) {
            if (stat.getDay() == bDate) {
                return stat;
            }
        }
        return null;
    }

    @Nullable
    public static Statystyka getStatsFromDay(ArrayList<Statystyka> stats, long date) {
        for (Statystyka stat : stats) {
            try {
                if (stat.getDay() == date) {
                    return stat;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

}