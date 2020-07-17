package pl.kamil0024.stats.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Statystyka {
    public Statystyka() {}

    public int zmutowanych = 0;
    public int zbanowanych = 0;

    public int usunietychWiadomosci = 0;
    public int napisanychWiadomosci = 0;

    public long day = 0;

}
