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

import com.google.gson.Gson;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;
import pl.kamil0024.api.Response;
import pl.kamil0024.api.redisstats.RedisStatsManager;
import pl.kamil0024.api.redisstats.config.ChatModStatsConfig;
import pl.kamil0024.api.redisstats.modules.CaseRedisManager;
import pl.kamil0024.core.logger.Log;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class RedisChatModStats implements HttpHandler {

    private final Random rand = new Random();
    RedisStatsManager redisStatsManager;

    public RedisChatModStats(RedisStatsManager redisStatsManager) {
        this.redisStatsManager = redisStatsManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!Response.checkIp(ex)) { return; }
        CaseRedisManager redis = redisStatsManager.getCaseRedisManager();
        List<RenderToCharts> charts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        try {
            boolean pa = Boolean.parseBoolean(ex.getQueryParameters().get("chatmod").getFirst());
            if (pa) {
                int rok = new DateTime().getYear();
                Map<Long, List<ChatModStatsConfig>> chatmodMiesiac = sortByKey(redis.getMapChatmodWMiesiacu());

                Map<String, List<Integer>> dataChatmodMiesiac = new HashMap<>();

                Map<Long, List<ChatModStatsConfig>> chatmodRok = sortByKey(redis.getMapChatmodWRoku());

                RenderToCharts rtc = new RenderToCharts();
                List<Datasets> datasets = new ArrayList<>();
                List<String> labels = new ArrayList<>();

                for (Map.Entry<Long, List<ChatModStatsConfig>> entry : chatmodRok.entrySet()) {
                    labels.add(entry.getKey() + "." + rok);
                    for (ChatModStatsConfig confEntry : entry.getValue()) {
                        List<Integer> data = dataChatmodMiesiac.getOrDefault(confEntry.getNick(), new ArrayList<>());
                        data.add(confEntry.getLiczbaKar());
                        dataChatmodMiesiac.put(confEntry.getNick(), data);
                    }
                }
                for (Map.Entry<String, List<Integer>> entry : dataChatmodMiesiac.entrySet()) {
                    if (entry.getValue().size() > labels.size()) {
                        continue;
                    }
                    datasets.add(new Datasets(entry.getKey(), randomColor(), entry.getValue()));
                }

                rtc.setLabels(labels);
                rtc.setDatasets(datasets);
                charts.add(rtc);
                Response.sendObjectResponse(ex, charts);
                return;
            }
        } catch (Exception ignored) { }

        try {
            Map<Long, Integer> karyWTygodniu = sortByKey(redis.getMapWTygodniu(), true);
            Map<Long, Integer> karyWRoku = sortByKey(redis.getMapKaryWRoku(), true);
            Map<Long, Integer> karyDzisiaj = redis.getMapOstatnieKary24h();
            Map<Long, Integer> karyWMiesiacu = sortByKey(redis.getMapKaryWMiesiacu(), false);

            charts.add(getCharts(karyWRoku, "rok"));
            charts.add(getCharts(sortByKey(karyDzisiaj, false), "dzisiaj"));

            charts.add(getCharts(sortByKey(karyWTygodniu, false), sdf));
            charts.add(getCharts(sortByKey(karyWMiesiacu, false), sdf));

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

    private RenderToCharts getCharts(Map<Long, Integer> map, String typ) {
        RenderToCharts renderToCharts = new RenderToCharts();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Datasets> datasets = new ArrayList<>();
        String kolor = "#1150d6";
        int rok = new DateTime().getYear();
        switch (typ) {
            case "rok":
                kolor = "#de23e8";
                for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                    labels.add(entry.getKey() + "." + rok);
                    data.add(entry.getValue());
                }
                break;
            case "dzisiaj":
                kolor = "#1ad97d";
                for (Map.Entry<Long, Integer> entry : map.entrySet()) {
                    labels.add(entry.getKey() + ":00");
                    data.add(entry.getValue());
                }
                break;
            default:
                throw new UnsupportedOperationException("Zły typ");
        }
        renderToCharts.setLabels(labels);
        datasets.add(new Datasets("Kary", kolor, data));
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
        List<Map.Entry<Long, Integer>> list =
                new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByKey());
        if (grow) Collections.reverse(list);
        Map<Long, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private Map<Long, List<ChatModStatsConfig>> sortByKey(Map<Long, List<ChatModStatsConfig>> map) {
        List<Map.Entry<Long, List<ChatModStatsConfig>>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByKey()));

        Map<Long, List<ChatModStatsConfig>> res = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ChatModStatsConfig>> entry : list) {
            res.put(entry.getKey(), entry.getValue());
        }
        return res;
    }

    private String randomColor() {
        int rand_num = rand.nextInt(0xffffff + 1);
        return String.format("#%06x", rand_num);
    }

}
