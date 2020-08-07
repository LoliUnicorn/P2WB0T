package pl.kamil0024.musicbot.core.util;

import java.util.Date;

public class Statyczne {

    private Statyczne() {}

    public static final String WERSJA;

    public static final String CORE_VERSION;

    public static final Date START_DATE;

    static {
        String version = Statyczne.class.getPackage().getImplementationVersion();

        if (version == null)
            WERSJA = "?.?.?";
        else
            WERSJA = version;

        CORE_VERSION = WERSJA;

        START_DATE = new Date();
    }

}
