package pl.kamil0024.weryfikacja;

import pl.kamil0024.api.APIModule;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.weryfikacja.listeners.WeryfikacjaListener;

public class WeryfikacjaModule implements Modul {

    private final APIModule apiModule;

    private boolean start = false;
    private WeryfikacjaListener weryfikacjaListener;
    private final MultiDao multiDao;

    public WeryfikacjaModule(APIModule apiModule, MultiDao multiDao) {
        this.apiModule = apiModule;
        this.multiDao = multiDao;
        this.weryfikacjaListener = new WeryfikacjaListener(apiModule, this.multiDao);
    }

    @Override
    public boolean startUp() {
        apiModule.getApi().addEventListener(weryfikacjaListener);
        return true;
    }

    @Override
    public boolean shutDown() {
        apiModule.getApi().removeEventListener(weryfikacjaListener);
        return true;
    }

    @Override
    public String getName() {
        return "weryfikacja";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = start;
    }

}
