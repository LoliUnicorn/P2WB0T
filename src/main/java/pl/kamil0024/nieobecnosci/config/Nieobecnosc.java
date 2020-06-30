package pl.kamil0024.nieobecnosci.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Nieobecnosc {

    public Nieobecnosc() { };

    private String userId;
    private int id;
    private String msgId = null;

    private long start;
    private String powod;
    private long end;

    private boolean aktywna = true;

}
