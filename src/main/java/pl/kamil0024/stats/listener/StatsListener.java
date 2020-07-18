package pl.kamil0024.stats.listener;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.stats.StatsModule;

import javax.annotation.Nonnull;
import java.util.Objects;

public class StatsListener extends ListenerAdapter {

    private StatsModule statsModule;

    public StatsListener(StatsModule statsModule) {
        this.statsModule = statsModule;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        Category category = event.getChannel().getParent();
        if (category == null || event.getAuthor().isBot() || event.getAuthor().isFake()) return;

        if (!Objects.requireNonNull(event.getMember()).getRoles().contains(event.getGuild().getRoleById(Ustawienia.instance.roles.chatMod))) return;

        if (category.getId().equals("425673488456482817") || category.getId().equals("506210855231291393") || category.getId().equals("494507499739676686")) {
            statsModule.getStatsCache().addNapisanychWiadomosci(event.getAuthor().getId(), 1);
        }

    }
}
