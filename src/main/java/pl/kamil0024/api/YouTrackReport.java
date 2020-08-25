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

package pl.kamil0024.api;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.logger.Log;

public class YouTrackReport implements HttpHandler {

    private final ShardManager api;

    public YouTrackReport(ShardManager api) {
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        for (HeaderValues header : ex.getRequestHeaders()) {
            Log.debug(header.getHeaderName().toString() + "=" + header.getFirst());
        }
        Response.sendResponse(ex, "Pomyślnie wysłano reporta!");
    }

}
