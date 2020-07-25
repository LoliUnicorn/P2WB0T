package pl.kamil0024.api.handlers;

import com.google.gson.Gson;
import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;

public class Karainfo implements HttpHandler {

    @Inject private CaseDao caseDao;

    public Karainfo(CaseDao caseDao) {
        this.caseDao = caseDao;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!CheckToken.checkToken(ex)) return;

        try {
            int id = Integer.parseInt(ex.getQueryParameters().get("id").getFirst());
            if (id <= 0) throw new NumberFormatException();

            Log.debug("id=" + id);
            CaseConfig cc = caseDao.get(id);
            Log.debug(new Gson().toJson(cc));
            if (cc.getKara() == null) throw new Exception();

            Response.sendObjectResponse(ex, cc);

        } catch (NumberFormatException e) {
            Response.sendErrorResponse(ex, "Złe ID", "ID kary jest puste lub nie jest liczbą");
        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Złe ID", "Nie ma kary o takim ID");
        }

    }

}
