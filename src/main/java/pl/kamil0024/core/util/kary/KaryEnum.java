package pl.kamil0024.core.util.kary;

public enum KaryEnum {
    KICK, BAN, MUTE, TEMPBAN, TEMPMUTE, UNMUTE, UNBAN;

    public static String getName(KaryEnum kara) {
        if (kara == KICK) return "Kick";
        if (kara == BAN) return "Ban";
        if (kara == MUTE) return "Mute";
        if (kara == TEMPBAN) return "Tempban";
        if (kara == TEMPMUTE) return "Tempmute";
        if (kara == UNBAN) return "Unban";
        if (kara == UNMUTE) return "Unmute";
        return String.valueOf(kara);
    }

    public static KaryEnum getKara(String kara) {
        kara = kara.toLowerCase();
        if (kara.equals("kick")) return KICK;
        if (kara.equals("ban")) return BAN;
        if (kara.equals("mute")) return MUTE;
        if (kara.equals("tempban")) return TEMPBAN;
        if (kara.equals("tempmute")) return TEMPMUTE;
        throw new UnsupportedOperationException("Typ kary " + kara + " nie istnieje!");
    }

}
