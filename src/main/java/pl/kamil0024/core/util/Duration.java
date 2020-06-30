package pl.kamil0024.core.util;

import lombok.Getter;
import lombok.Setter;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.bdate.util.BLanguage;
import pl.kamil0024.bdate.util.Nullable;
import pl.kamil0024.bdate.util.Time;

public class Duration {

    @Getter @Setter private Long now;
    @Getter @Setter private Long date;

    public Duration() {
        now = new BDate().getTimestamp();
    }

    public Duration(Long now) {
        this.now = now;
    }

    @Nullable
    public Long parseLong(String s) {
        if (s == null) return null;
        String split = null;
        Time time = null;

        if (s.endsWith("d")) {
            split = "d";
            time = Time.DAY;
        } else if (s.endsWith("s")) {
            split = "s";
            time = Time.SECOND;
        } else if (s.endsWith("m")) {
            split = "m";
            time = Time.MINUTE;
        } else if (s.endsWith("h")) {
            split = "h";
            time = Time.HOUR;
        }
        if (split == null) return null;

        String[] czas = s.split(split);
        long longg;

        try {
            long l = Long.parseLong(czas[0]);
            if (l <= 0) return null;
            longg = l;
        } catch (NumberFormatException e) { return null; }

        return new BDate(now, getLang()).add(longg, time).getTimestamp();
    }

    public String parseString() {
        return new BDate(now, getLang()).difference(date);
    }

    private static BLanguage getLang() {
        BLanguage lang = new BLanguage();
        lang.setSecond("sek.");
        lang.setMinute("min.");
        lang.setHour("godz.");
        lang.setDay("d.");
        return lang;
    }

}