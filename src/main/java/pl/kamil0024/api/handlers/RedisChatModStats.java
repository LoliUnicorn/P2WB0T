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
import pl.kamil0024.api.Response;
import pl.kamil0024.api.redisstats.RedisStatsManager;
import pl.kamil0024.api.redisstats.modules.CaseRedisManager;

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

        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy");
        CaseRedisManager redis = redisStatsManager.getCaseRedisManager();
        List<RenderToCharts> charts = new ArrayList<>();

        Map<String, Integer> karyWTygodniu = redis.getRedisWTygodniu().asMap();
//        Map<String, Integer> karyWRoku = redis.getRedisKaryWRoku().asMap();
//        Map<String, Integer> karyDzisiaj = redis.getRedisOstatnieKary24h().asMap();

        RenderToCharts renderToCharts = new RenderToCharts();
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : karyWTygodniu.entrySet()) {
            String[] key = entry.getKey().split(":");
            Date d = new Date(Long.parseLong(key[key.length - 1]));
            labels.add(sfd.format(d));
            data.add(entry.getValue());
        }
        renderToCharts.setLabels(labels);
        renderToCharts.getDatasets().add(new Datasets("Lista kar", "#1150d6", data));
        charts.add(renderToCharts);

        Response.sendObjectResponse(ex, charts);
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
