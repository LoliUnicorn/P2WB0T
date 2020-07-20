package pl.kamil0024.commands.kolkoikrzyzyk.entites;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.entities.TextChannel;

@Data
@AllArgsConstructor
public class Zaproszenie {
    public Zaproszenie() {}

    private String zapraszajacy;
    private String zapraszajaGo;
    private TextChannel channel;
    private int id;
    private long kiedy;

}
