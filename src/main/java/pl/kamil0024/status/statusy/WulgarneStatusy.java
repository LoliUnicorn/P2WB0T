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

package pl.kamil0024.status.statusy;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.RichPresence;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class WulgarneStatusy extends ListenerAdapter {

    private static final String HTTPS = "\\w+:\\/{2}[\\d\\w-]+(\\.[\\d\\w-]+)*(?:(?:[^\\s/]*))*";
    private static final String HTTP = "([0-9a-z_-]+\\.)+(com|infonet|net|org|pro|de|ggmc|md|me|tt|tv|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt)";

    public static List<String> getAvtivity(Member mem) {
        List<String> list = new ArrayList<>();
        try {
            for (Activity act : mem.getActivities()) {
                list.add(act.getName());
                if (act.isRich()) {
                    RichPresence rp = act.asRichPresence();
                    try {
                        //noinspection ConstantConditions
                        list.add(rp.getState());
                        list.add(rp.getDetails());
                    } catch (NullPointerException ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int xd = 0; xd < 5; xd++) {
            list.remove(null);
        }
        return list;
    }

    @Nullable
    public static String containsLink(List<String> list) {
        for (String s : list) {
            if (s != null && !s.isEmpty() && !s.toLowerCase().contains("gram na")) {
                s = s.toLowerCase().replaceAll("derpmc.pl", "tak")
                        .replaceAll("feerko.pl", "tak")
                        .replaceAll("hajsmc.pl", "tak")
                        .replaceAll("roizy.pl", "tak")
                        .replaceAll("hypixel\\.net", "tak")
                        .replaceAll("blazingpack\\.pl", "tak")
                        .replaceAll("1.8.8", "tak")
                        .replaceAll("\\.by", "tak")
                        .replace(".by", "tak")
                        .replaceAll("blazingpack\\.pl", "tak");
                String xd = s.replaceAll(HTTP, "CzemuTutajJestJakisJebanyInvite");
                String xdd = s.replaceAll(HTTPS, "CzemuTutajJestJakisJebanyInvite");
                if (xd.contains("CzemuTutajJestJakisJebanyInvite") || xdd.contains("CzemuTutajJestJakisJebanyInvite")) return s;
            }
        }
        return null;
    }

    @Nullable
    public static String containsSwear(List<String> list) {
        File file = new File("/home/kamil/P2WB0T/plugins/Skript/scripts/P2WB0T/commands/!Utils/Przeklenstwa.api");
        if (!file.exists()) {
            System.out.println("[ERROR] plik do przeklenstw nie istnieje");
            return null;
        }
        List<String> przeklenstwa = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) { przeklenstwa.add(line); }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        for (String s : list) {
            if (s != null && !s.isEmpty()) {
                for (String split : s.toLowerCase().split(" ")) {
                    if (przeklenstwa.contains(split)) return split;
                }
            }
        }
        return null;
    }

    @Nullable
    public static String getPrivateChannel(Member mem) {
        List<String> rolesId = new ArrayList<>();
        mem.getRoles().forEach(r -> rolesId.add(r.getId()));

        if (rolesId.contains("425671314536398848")) return "563045256833269810"; // vip

        if (rolesId.contains("425671087993651201")) return "563045169264590867"; // vip+

        if (rolesId.contains("425670812691857408")) return "563044944785309746"; // mvp

        if (rolesId.contains("425670785558904833") || rolesId.contains("425670797630373888")) return "506212708652417024";
        return null;
    }

}
