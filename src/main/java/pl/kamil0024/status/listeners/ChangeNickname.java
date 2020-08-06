package pl.kamil0024.status.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;

public class ChangeNickname extends ListenerAdapter {

    public ChangeNickname() {}

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent e) {
        Guild g = e.getJDA().getGuildById(Ustawienia.instance.bot.guildId);
        if (g == null) throw new NullPointerException("gilda jest nullem");

        Member mem = g.retrieveMemberById(e.getUser().getId()).complete();
        if (mem == null) return;

        if (mem.getNickname() == null) {
            g.modifyNickname(mem, e.getOldName()).queue();
        }

    }
}