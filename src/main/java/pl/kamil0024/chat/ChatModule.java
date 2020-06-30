package pl.kamil0024.chat;

import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import pl.kamil0024.chat.listener.ChatListener;
import pl.kamil0024.chat.listener.KaryListener;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.util.kary.KaryJSON;

public class ChatModule implements Modul {

    @Inject private JDA api;
    @Inject private KaryJSON karyJSON;
    @Inject private CaseDao caseDao;
    @Inject private ModLog modLog;

    private boolean start = false;
    private ChatListener chatListener;
    private KaryListener karyListener;

    public ChatModule(JDA api, KaryJSON karyJSON, CaseDao caseDao, ModLog modLog) {
        this.api = api;
        this.karyJSON = karyJSON;
        this.modLog = modLog;
        this.caseDao = caseDao;
    }

    @Override
    public boolean startUp() {
        this.chatListener = new ChatListener(api, karyJSON, caseDao, modLog);
        this.karyListener = new KaryListener(karyJSON, caseDao, modLog);
        api.addEventListener(chatListener, karyListener);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        KaryListener.getEmbedy().clear();
        api.removeEventListener(chatListener, karyListener);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "chat";
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
