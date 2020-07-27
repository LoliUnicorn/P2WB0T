package pl.kamil0024.core.util.kary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.json.JSONTokener;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.logger.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class KaryJSON {

    private final File file;

    private ArrayList<Kara> kary;

    InputStream is;
    JSONTokener tokener;
    JSONObject object = null;

    @SneakyThrows
    public KaryJSON() {
        this.kary = new ArrayList<>();

        URL res = Main.class.getClassLoader().getResource("kary.json");
        if (res == null) {
            Log.newError("Plik kary.json jest nullem");
            throw new NullPointerException("Plik kary.json jest nullem");
        }

        this.file = new File(res.getFile());

        if (!file.exists()) {
            String msg = "Plik " + file.getName() + " nie istnieje";
            Log.newError(msg);
            throw new UnsupportedOperationException(msg);
        }
        try {
            this.is = new FileInputStream(file);
            this.tokener = new JSONTokener(is);
            this.object = new JSONObject(tokener).getJSONObject("list");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.newError("Przy ladowaniu kary \n" + e.getLocalizedMessage());
        }
        loadKary();
    }

    @SneakyThrows
    private void loadKary() {
        if (object == null) throw new UnsupportedOperationException("object przy ladowaniu kary jest nullem");
        
        Iterator<String> keys = object.keys();
        ArrayList<String> ids = new ArrayList<>();

        while (keys.hasNext()) { ids.add(keys.next()); }

        for (int i = 1; i < ids.size(); i++) {
            JSONObject obj = object.getJSONObject(String.valueOf(i));
            String powod = repla(obj.getString("name"));
            ArrayList<Tiery> tieryList = new ArrayList<>();

            Iterator<String> tierKeys = obj.keys();
            ArrayList<String> tierIds = new ArrayList<>();
            while (tierKeys.hasNext()) { tierIds.add(tierKeys.next()); }
            for (int ii = 1; ii < tierIds.size(); ii++) {
                try {
                    JSONObject tier = obj.getJSONObject("tier_" + ii);
                    tieryList.add(new Tiery(tier.getInt("maxWarns"), tier.getString("time"), KaryEnum.getKara(tier.getString("type"))));
                } catch (Exception ignored) {}
            }
            getKary().add(new Kara(i, powod, tieryList));
        }

    }

    @Nullable
    public Kara getByName(String name) {
        for (Kara kara : getKary()) {
            if (kara.getPowod().toLowerCase().equals(name.toLowerCase())) return kara;
        }
        return null;
    }

    private static String repla(String s) {
        return s.replaceAll("Ä…", "ą").
                replaceAll("Ä‡", "ć").
                replaceAll("Ä™", "ę").
                replaceAll("Ĺ‚", "ł").
                replaceAll("Ĺ„", "ń").
                replaceAll("Ăł", "ó").
                replaceAll("Ĺ›", "ś").
                replaceAll("Ĺş", "ź").
                replaceAll("ĹĽ", "ż");
    }

    @Data
    @AllArgsConstructor
    public class Kara {
        public Kara() {}

        private int id;
        private String powod;
        private List<Tiery> tiery;

    }

    @Data
    @AllArgsConstructor
    public class Tiery {
        private int maxWarns;
        private String duration;
        private KaryEnum type;
    }

}
