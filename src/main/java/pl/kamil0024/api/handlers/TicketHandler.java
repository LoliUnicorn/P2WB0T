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
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.TicketDao;
import pl.kamil0024.core.database.config.TicketConfig;

public class TicketHandler implements HttpHandler {

    private final TicketDao ticketDao;
    private final int type;

    public TicketHandler(TicketDao ticketDao, int type) {
        this.ticketDao = ticketDao;
        this.type = type;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) {
        if (!Response.checkIp(ex)) { return; }

        if (type == 0) {
            try {
                String id = ex.getRequestHeaders().get("id").getFirst();
                String admId = ex.getRequestHeaders().get("admId").getFirst();
                String userId = ex.getRequestHeaders().get("userId").getFirst();
                String userNick = ex.getRequestHeaders().get("userNick").getFirst();
                int ocena = Integer.parseInt(ex.getRequestHeaders().get("ocena").getFirst());
                String temat = ex.getRequestHeaders().get("temat").getFirst();
                boolean problemRozwiazany = Boolean.parseBoolean(ex.getRequestHeaders().get("problemRozwiazany").getFirst());
                String uwaga = null;
                try {
                    uwaga = ex.getRequestHeaders().get("uwaga").getFirst();
                } catch (NullPointerException ignored) {}
                TicketConfig tc = new TicketConfig(id);
                tc.setAdmId(admId);
                tc.setUserId(userId);
                tc.setUserNick(userNick);
                tc.setOcena(ocena);
                tc.setTemat(temat);
                tc.setProblemRozwiazany(problemRozwiazany);
                if (uwaga != null) tc.setUwaga(uwaga);
                if (ticketDao.get(id).getOcena() == -1) {
                    throw new UnsupportedOperationException("ID nie może się doublować!");
                }
                Response.sendResponse(ex, "Pomyślnie zapisano");
                ticketDao.save(tc);
            } catch (Exception e) {
                Response.sendErrorResponse(ex, "Błąd!", "Nie udało się wysłać requesta: " + e.getMessage());
            }
        }

        try {
            String id = ex.getQueryParameters().get("id").getFirst();
            switch (type) {
                case 1:
                    Response.sendObjectResponse(ex, ticketDao.get(id));
                    break;
                case 2:
                    Response.sendObjectResponse(ex, ticketDao.getByNick(id));
                    break;
                case 3:
                    Response.sendObjectResponse(ex, ticketDao.getById(id));
                    break;
                default:
                    throw new UnsupportedOperationException("Zły int type");
            }
        } catch (Exception e) {
            Response.sendErrorResponse(ex, "Błąd!", "Nie udało się wysłać requesta: " + e.getMessage());
        }
    }

}
