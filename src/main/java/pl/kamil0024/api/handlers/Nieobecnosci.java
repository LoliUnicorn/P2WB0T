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

package pl.kamil0024.api.handlers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.NieobecnosciDao;
import pl.kamil0024.core.database.config.NieobecnosciConfig;
import pl.kamil0024.core.database.config.UserinfoConfig;
import pl.kamil0024.nieobecnosci.config.Nieobecnosc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Nieobecnosci implements HttpHandler {

    @Inject private final NieobecnosciDao nieobecnosciDao;
    @Inject private final APIModule api;

    public Nieobecnosci(NieobecnosciDao nieobecnosciDao, APIModule apiModule) {
        this.nieobecnosciDao = nieobecnosciDao;
        this.api = apiModule;
    }


    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!CheckToken.checkToken(ex)) return;

        try {
            String parm = ex.getQueryParameters().get("data").getFirst();
            if (parm.isEmpty()) {
                Response.sendErrorResponse(ex, "Zły parametr", "Paramatr data musi mieć wartość `all`, `aktywne` lub nick gracza");
                return;
            }

            if (parm.equals("all")) {
                List<NieobecnosciConfig> nb = nieobecnosciDao.getAll();
                HashMap<String, List<Nieobecnosc>> formated = new HashMap<>();

                if (nb.isEmpty()) {
                    Response.sendErrorResponse(ex, "Pusta lista", "Nikt jeszcze nie zgłaszał nieobecności");
                    return;
                }

                for (NieobecnosciConfig config : nb) {
                    UserinfoConfig uic = api.getUserConfig(config.getId());
                    for (Nieobecnosc nieobecnosc : config.getNieobecnosc()) {
                        List<Nieobecnosc> lista = formated.getOrDefault(uic.getWhateverName(), new ArrayList<>());
                        lista.add(format(nieobecnosc, api));
                        formated.put(uic.getWhateverName(), lista);
                    }
                }

                Response.sendObjectResponse(ex, formated);
                return;
            }

            if (parm.equals("aktywne")) {
                ArrayList<Nieobecnosc> nb = nieobecnosciDao.getAllAktywne();
                ArrayList<Nieobecnosc> formated = new ArrayList<>();

                if (nb.isEmpty()) {
                    Response.sendErrorResponse(ex, "Pusta lista", "Nikt jeszcze nie zgłaszał nieobecności");
                    return;
                }

                for (Nieobecnosc nieobecnosc : nb) { formated.add(format(nieobecnosc, api)); }

                Response.sendObjectResponse(ex, formated);
                return;
            }

            List<NieobecnosciConfig> nb = nieobecnosciDao.getAll();
            List<Nieobecnosc> jegoUrlopy = new ArrayList<>();

            for (NieobecnosciConfig config : nb) {
                UserinfoConfig uc = api.getUserConfig(config.getId());
                try {
                    if (uc.getMcNick().split(" ")[1].equalsIgnoreCase(parm)) {
                        for (Nieobecnosc nieobecnosc : config.getNieobecnosc()) {
                            jegoUrlopy.add(format(nieobecnosc, api));
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (jegoUrlopy.isEmpty()) {
                Response.sendErrorResponse(ex, "Pusta lista", "Ten nick nie ma żadnych nieobecności");
                return;
            }

            Response.sendObjectResponse(ex, jegoUrlopy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Nieobecnosc format(Nieobecnosc urlop, APIModule api) {
        UserinfoConfig uc = api.getUserConfig(urlop.getUserId());

        urlop.setUserId(uc.getMcNick() == null ? uc.getFullname() : uc.getMcNick());
        urlop.setMsgId("https://discord.com/channels/422016694408577025/687775040065896495/" + urlop.getMsgId());
        return urlop;
    }


}
