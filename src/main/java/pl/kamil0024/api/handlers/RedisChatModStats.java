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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;
import pl.kamil0024.api.Response;
import pl.kamil0024.api.redisstats.RedisStatsManager;
import pl.kamil0024.api.redisstats.modules.CaseRedisManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class RedisChatModStats implements HttpHandler {

    RedisStatsManager redisStatsManager;

    public RedisChatModStats(RedisStatsManager redisStatsManager) {
        this.redisStatsManager = redisStatsManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!Response.checkIp(ex)) { return; }

        try {
            CaseRedisManager redis = redisStatsManager.getCaseRedisManager();
            List<RenderToCharts> charts = new ArrayList<>();

            Map<Long, Integer> karyWTygodniu = sortByKey(redis.getMapWTygodniu(), true);
            Map<Integer, Integer> karyWRoku = redis.getMapKaryWRoku();
            Map<Integer, Integer> karyDzisiaj = redis.getMapOstatnieKary24h();
            Map<Long, Integer> karyWMiesiacu = sortByKey(redis.getMapKaryWMiesiacu(), false);

            charts.add(getCharts(karyWRoku, "rok"));
            charts.add(getCharts(karyDzisiaj, "dzisiaj"));
            
            charts.add(getCharts(sortByKey(karyWTygodniu, false), new SimpleDateFormat("dd.MM.yyyy")));
            charts.add(getCharts(sortByKey(karyWMiesiacu, false), new SimpleDateFormat("dd.MM.yyyy")));

            Response.sendObjectResponse(ex, charts);
        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Błąd!", "Nie udało się wysłać requesta");
        }
    }

    @Data
    @AllArgsConstructor
    @SuppressWarnings("InnerClassMayBeStatic")
    private class RenderToCharts {
        public RenderToCharts() { }

        private List<String> labels = new ArrayList<>();
        private List<Datasets> datasets;
    }

    @Data
    @AllArgsConstructor
    private static class Datasets {
        private String label;
        private String color;
        private List<Integer> data;
    }

    private RenderToCharts getCharts(Map<Integer, Integer> map, String typ) {
        RenderToCharts renderToCharts = new RenderToCharts();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Datasets> datasets = new ArrayList<>();
        String kolor = "#1150d6";
        int rok = new DateTime().getYear();
        switch (typ) {
            case "rok":
                kolor = "#de23e8";
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    labels.add(entry.getKey() + "." + rok);
                    data.add(entry.getValue());
                }
                break;
            case "dzisiaj":
                kolor = "#1ad97d";
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    labels.add(entry.getKey() + ":00");
                    data.add(entry.getValue());
                }
                break;
            default:
                throw new UnsupportedOperationException("Zły typ");
        }
        renderToCharts.setLabels(labels);
        datasets.add(new Datasets("Lista kar", kolor, data));
        renderToCharts.setDatasets(datasets);
        return renderToCharts;
    }

    private RenderToCharts getCharts(Map<Long, Integer> map, SimpleDateFormat sfd) {
        RenderToCharts renderToCharts = new RenderToCharts();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Datasets> datasets = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Date d = new Date(entry.getKey());
            labels.add(sfd.format(d));
            data.add(entry.getValue());
        }
        renderToCharts.setLabels(labels);
        datasets.add(new Datasets("Kary", "#1150d6", data));
        renderToCharts.setDatasets(datasets);
        return renderToCharts;
    }

    private Map<Long, Integer> sortByKey(Map<Long, Integer> hm, boolean grow) {
        List<Map.Entry<Long, Integer> > list =
                new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByKey());
        if (grow) Collections.reverse(list);
        Map<Long, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}
