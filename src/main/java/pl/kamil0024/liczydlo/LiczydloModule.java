package pl.kamil0024.liczydlo;

import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import pl.kamil0024.core.module.Modul;

public class LiczydloModule implements Modul {

    @Inject JDA api;

    private boolean start = false;
    private LiczydloListener liczydloListener;

    public LiczydloModule(JDA api) {
        this.api = api;
    }

    @Override
    public boolean startUp() {
        this.liczydloListener = new LiczydloListener(api);
        api.addEventListener(liczydloListener);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(liczydloListener);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "liczydlo";
    }

    @Override
    public boolean isStart() {
        return this.start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }

}
