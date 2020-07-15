package pl.kamil0024.core.command.enums;

import lombok.Getter;

@SuppressWarnings("unused")
public enum PermLevel {

    MEMBER(0, "permlvl.member"),
    HELPER(1, "permlvl.helper"),
    MODERATOR(2, "permlvl.mod"),
    ADMINISTRATOR(3, "permlvl.adm"),
    DEVELOPER(10, "permlvl.dev");

    @Getter private final int numer;
    @Getter private final String tranlsateKey;

    PermLevel(int numer, String tranlsateKey) {
        this.numer = numer;
        this.tranlsateKey = tranlsateKey;
    }

    public static PermLevel getPermLevel(int numer) {
        if (numer == 0) return MEMBER;
        if (numer == 1) return HELPER;
        if (numer == 2) return MODERATOR;
        if (numer == 3) return ADMINISTRATOR;
        if (numer == 10) return DEVELOPER;
        throw new IllegalArgumentException("Nieprawidłowy poziom!");
    }

}
