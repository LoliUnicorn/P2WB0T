package pl.kamil0024.core.listener;

import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.core.logger.Log;

import javax.annotation.Nonnull;

public class ExceptionListener extends ListenerAdapter {

    public ExceptionListener() { }

    @Override
    public void onException(@Nonnull ExceptionEvent event) {
        Log.newError(event.getCause());
    }

}
