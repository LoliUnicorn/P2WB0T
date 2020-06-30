package pl.kamil0024.core.util;

import java.util.Date;

public class Statyczne {

    private Statyczne() {}

    public static final String WERSJA;

    public static final String CORE_VERSION;

    public static final Date startDate;

    static {
        String version = Statyczne.class.getPackage().getImplementationVersion();

        if (version == null)
            WERSJA = "?.?.?";
        else
            WERSJA = version;

        CORE_VERSION = WERSJA;

        startDate = new Date();
    }

}
