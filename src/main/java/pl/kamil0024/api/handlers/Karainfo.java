package pl.kamil0024.api.handlers;

import com.google.inject.Inject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import pl.kamil0024.api.APIModule;
import pl.kamil0024.api.Response;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.database.config.UserinfoConfig;

public class Karainfo implements HttpHandler {

    @Inject private CaseDao caseDao;
    @Inject private APIModule api;

    public Karainfo(CaseDao caseDao, APIModule api) {
        this.caseDao = caseDao;
        this.api = api;
    }

    @Override
    public void handleRequest(HttpServerExchange ex) throws Exception {
        if (!CheckToken.checkToken(ex)) return;

        try {
            int id = Integer.parseInt(ex.getQueryParameters().get("id").getFirst());
            if (id <= 0) throw new NumberFormatException();

            CaseConfig cc = caseDao.get(id);
            if (cc.getKara() == null) throw new Exception();

            Response.sendObjectResponse(ex, format(cc, api));

        } catch (NumberFormatException e) {
            Response.sendErrorResponse(ex, "Złe ID", "ID kary jest puste lub nie jest liczbą");
        } catch (Exception e) {
            e.printStackTrace();
            Response.sendErrorResponse(ex, "Złe ID", "Nie ma kary o takim ID");
        }

    }
    
    public static CaseConfig format(CaseConfig cc, APIModule api) {
        UserinfoConfig userc = api.getUserConfig(cc.getKara().getKaranyId());
        UserinfoConfig admc = api.getUserConfig(cc.getKara().getAdmId());
        cc.getKara().setMessageUrl("https://discordapp.com/channels/" + cc.getKara().getMessageUrl());

        cc.getKara().setKaranyId(userc.getMcNick() == null ? userc.getFullname() : userc.getMcNick());
        cc.getKara().setAdmId(admc.getMcNick() == null ? admc.getFullname() : admc.getMcNick());
        return cc;
    }

}
