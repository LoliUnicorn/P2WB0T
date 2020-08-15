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
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;

import java.util.*;

public class Listakar implements HttpHandler {

    @Inject private CaseDao caseDao;
    @Inject private APIModule api;

    public Listakar(CaseDao caseDao, APIModule apiModule) {
        this.caseDao = caseDao;
        this.api = apiModule;
    }


    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!CheckToken.checkToken(ex)) return;

        String nick = ex.getQueryParameters().get("nick").getFirst();
        if (nick.isEmpty()) {
            Response.sendErrorResponse(ex,"Zły nick", "Nick jest pusty?");
            return;
        }

        try {
            HashMap<CaseConfig, Integer> karyMap = new HashMap<>();
            List<CaseConfig> kary = new ArrayList<>();

            caseDao.getAllNick(nick).forEach(ccase -> {
                CaseConfig formated = Karainfo.format(ccase, api);
                karyMap.put(formated, formated.getKara().getKaraId());
            });
            for (Map.Entry<CaseConfig, Integer> entry : sortByValue(karyMap).entrySet()) {
                kary.add(entry.getKey());
            }

            if (kary.isEmpty()) {
                Response.sendErrorResponse(ex,"Zły nick", "Ten nick nie ma żadnej kary");
                return;
            }

            Response.sendObjectResponse(ex, kary);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private HashMap<CaseConfig, Integer> sortByValue(HashMap<CaseConfig, Integer> hm) {
        List<Map.Entry<CaseConfig, Integer> > list =
                new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        HashMap<CaseConfig, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<CaseConfig, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
