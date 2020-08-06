package pl.kamil0024.weryfikacja;

import pl.kamil0024.api.APIModule;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.status.listeners.ChangeNickname;
import pl.kamil0024.weryfikacja.listeners.WeryfikacjaListener;

public class WeryfikacjaModule implements Modul {

    private final APIModule apiModule;
    private final MultiDao multiDao;
    private final ModLog modLog;

    private boolean start = false;
    private WeryfikacjaListener weryfikacjaListener;
    private ChangeNickname changeNickname;

    public WeryfikacjaModule(APIModule apiModule, MultiDao multiDao, ModLog modLog) {
        this.apiModule = apiModule;
        this.multiDao = multiDao;
        this.modLog = modLog;
        this.weryfikacjaListener = new WeryfikacjaListener(apiModule, this.multiDao, modLog);
    }

    @Override
    public boolean startUp() {
        this.changeNickname = new ChangeNickname();
        apiModule.getApi().addEventListener(weryfikacjaListener, changeNickname);
        return true;
    }

    @Override
    public boolean shutDown() {
        apiModule.getApi().removeEventListener(weryfikacjaListener, changeNickname);
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
        this.start = bol;
    }

}
