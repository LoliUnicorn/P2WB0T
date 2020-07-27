package pl.kamil0024.core.util;

import lombok.Getter;
import lombok.Setter;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.logger.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

public class Tlumaczenia {

    @Getter private Properties languages = getProp();
    @Getter @Setter private String lang;

    public Tlumaczenia() { }

    public Properties getProp() {
        Properties p = new Properties();

        URL res = Main.class.getClassLoader().getResource(Ustawienia.instance.language.toLowerCase() + ".properties");
        if (res == null) {
            Log.newError("Plik .properties jest nullem");
            throw new NullPointerException("Plik .properties jest nullem");
        }
        File file = new File(res.getFile());
        if (!file.exists()) {
            Log.newError("Plik .properties nie istnieje!");
            throw new NullPointerException("Plik .properties nie istnieje!");
        }

        try {
            InputStream input = new FileInputStream(file);
            p.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }
    
    public boolean load() {
        languages = getProp();
        Log.debug(get("translation.load"));
        return true;
    }

    public String get(String key) {
        if (languages == null) throw new NullPointerException("getProp() == null");
        String a = repla(languages.getProperty(key, key));
        if (a.equals(key)) Log.newError("Key %s nie jest przetłumaczony", key);
        return a;
    }

    public String get(String key, Object... toReplace) {
        if (languages == null) throw new NullPointerException("getProp() == null");
        String property = get(key);
        ArrayList<String> parsedArray = new ArrayList<>();
        for (Object k : toReplace) {
            parsedArray.add(k.toString());
        }
        return repla(String.format(property, parsedArray.toArray()));
    }

    public String get(String key, String... toReplace) {
        if (languages == null) throw new NullPointerException("getProp() == null");
        return repla(String.format(get(key), (Object[]) toReplace));
    }

    public static String repla(String key) {
        return key.replaceAll("Ä\u0085", "ą").
                replaceAll("Ä\u0087", "ć").
                replaceAll("Ä\u0099", "ę").
                replaceAll("Å\u0082", "ł").
                replaceAll("Å\u0084", "ń").
                replaceAll("Ã³", "ó").
                replaceAll("Å\u009B", "ś").
                replaceAll("Åº", "ź").
                replaceAll("Å¼", "ż");
    }

}
