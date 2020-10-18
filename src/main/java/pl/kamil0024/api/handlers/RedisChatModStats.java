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
import pl.kamil0024.api.Response;
import pl.kamil0024.api.redisstats.RedisStatsManager;
import pl.kamil0024.api.redisstats.modules.CaseRedisManager;
import pl.kamil0024.core.logger.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RedisChatModStats implements HttpHandler {

    RedisStatsManager redisStatsManager;

    public RedisChatModStats(RedisStatsManager redisStatsManager) {
        this.redisStatsManager = redisStatsManager;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!Response.checkIp(ex)) { return; }

        try {
            SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
            CaseRedisManager redis = redisStatsManager.getCaseRedisManager();
            List<RenderToCharts> charts = new ArrayList<>();

            Map<Long, Integer> karyWTygodniu = redis.getMapWTygodniu();
    //        Map<String, Integer> karyWRoku = redis.getRedisKaryWRoku().asMap();
    //        Map<String, Integer> karyDzisiaj = redis.getRedisOstatnieKary24h().asMap();

            RenderToCharts renderToCharts = new RenderToCharts();
            List<String> labels = new ArrayList<>();
            List<Integer> data = new ArrayList<>();
            List<Datasets> datasets = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : karyWTygodniu.entrySet()) {
                Log.debug("key: " + entry.getKey());
                Log.debug("value: " + entry.getValue());
                Date d = new Date(entry.getKey());
                labels.add(sfd.format(d));
                data.add(entry.getValue());
            }
            renderToCharts.setLabels(labels);
            datasets.add(new Datasets("Lista kar", "#1150d6", data));
            renderToCharts.setDatasets(datasets);
            charts.add(renderToCharts);

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

}
