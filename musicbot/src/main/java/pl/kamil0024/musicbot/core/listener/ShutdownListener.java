package pl.kamil0024.musicbot.core.listener;

import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.musicbot.core.Ustawienia;
import pl.kamil0024.musicbot.core.util.NetworkUtil;

import javax.annotation.Nonnull;

public class ShutdownListener extends ListenerAdapter {

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        try {
            NetworkUtil.getJson(String.format("http://0.0.0.0:%s/api/musicbot/shutdown/%s", Ustawienia.instance.api.mainPort, Ustawienia.instance.api.port));
        } catch (Exception ignored) {}
    }

}
