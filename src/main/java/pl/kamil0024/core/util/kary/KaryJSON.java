/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.core.util.kary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.logger.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class KaryJSON {

    private static Logger logger = LoggerFactory.getLogger(KaryJSON.class);

    private ArrayList<Kara> kary;

    InputStream is;
    JSONTokener tokener;
    JSONObject object = null;

    @SneakyThrows
    public KaryJSON() {
        this.kary = new ArrayList<>();

        try {
            this.is = Main.class.getClassLoader().getResourceAsStream("kary.json");
            if (is == null) throw new NullPointerException("kary.json jest nullem");
            this.tokener = new JSONTokener(is);
            this.object = new JSONObject(tokener).getJSONObject("list");
        } catch (Exception e) {
            e.printStackTrace();
            Log.newError("Przy ladowaniu kary \n" + e.getLocalizedMessage(), KaryJSON.class);
        }
        loadKary();
    }

    @SneakyThrows
    private void loadKary() {
        if (object == null) throw new UnsupportedOperationException("object przy ladowaniu kary jest nullem");
        
        Iterator<String> keys = object.keys();
        ArrayList<String> ids = new ArrayList<>();
        while (keys.hasNext()) { ids.add(keys.next()); }
        logger.debug("Ładuje kary {}", ids.size());

        for (int i = 1; i < ids.size(); i++) {
            try {
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
                Kara kara = new Kara(i, powod, tieryList);
                logger.debug("------------------------");
                logger.debug("ID: " + kara.getId());
                logger.debug("Powod: " + kara.getPowod());
                for (Tiery entry : kara.getTiery()) {
                    logger.debug("  Duration:" + entry.getDuration());
                    logger.debug("  Type:" + entry.getType());
                    logger.debug("  MaxWarns:" + entry.getMaxWarns());
                }
                logger.debug("------------------------");
                getKary().add(kara);
            } catch (Exception e) {
                Log.newError("Nie udało się załadować kary nr:" + i, getClass());
                Log.newError(e, getClass());
            }
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
