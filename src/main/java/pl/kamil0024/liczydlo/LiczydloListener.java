package pl.kamil0024.liczydlo;

import com.google.inject.Inject;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.logger.Log;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LiczydloListener extends ListenerAdapter {

    @Inject private ShardManager api;

    TextChannel txt = null;

    public LiczydloListener(ShardManager api) {
        this.api = api;
        txt = api.getTextChannelById(Ustawienia.instance.channel.liczek);
        if (txt == null) api.removeEventListener(this);
        Log.newError("Kanał do liczenia jest nullem");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(Ustawienia.instance.channel.liczek)) return;

        if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage()) {
            event.getMessage().delete().queue();
            return;
        }

        String msg = event.getMessage().getContentRaw();
        if (msg.isEmpty()) {
            event.getMessage().delete().queue();
            return;
        }

        int liczba;

        try {
            liczba = Integer.parseInt(msg);
        } catch (NumberFormatException e) {
            event.getMessage().delete().queue();
            return;
        }

        List<Message> messages = getHistoryList(event.getChannel());
        int kiedysMessage = Integer.parseInt(messages.get(1).getContentRaw());

        if (messages.size() < 2 || liczba != kiedysMessage+1 || messages.get(1).getAuthor().getId().equals(event.getAuthor().getId())) {
            event.getMessage().delete().queue();
            return;
        }

        updateTopic(event.getChannel());
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        if (!event.getChannel().getId().equals(Ustawienia.instance.channel.liczek)) return;
        event.getMessage().delete().complete();
        updateTopic(event.getChannel());
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (!event.getChannel().getId().equals(Ustawienia.instance.channel.liczek)) return;
        updateTopic(event.getChannel());
    }


    private List<Message> getHistoryList(TextChannel txt) {
        List<Message> msgs = new ArrayList<>();
        for (Message msg : txt.getIterableHistory().stream().limit(3).collect(Collectors.toList())) {
            if (!msg.isEdited()) {
                msgs.add(msg);
                if (msgs.size() == 2) break;
            } else msg.delete().queue();
        }
        return msgs;
    }

    private void updateTopic(TextChannel txt) {
        Message msg = getHistoryList(txt).get(0);
        try {
            int liczba = Integer.parseInt(msg.getContentDisplay());
            String format = "Ostatnia zarejestrowana osoba: %s\nNastępna wiadomość powinna mieć treść: %s";
            txt.getManager().setTopic(String.format(format, msg.getAuthor().getAsMention(), liczba+1)).queue();
        } catch (Exception ignored) {}
    }

}
