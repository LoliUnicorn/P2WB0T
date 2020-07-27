package pl.kamil0024.commands.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.MultiDao;
import pl.kamil0024.core.database.config.MultiConfig;
import pl.kamil0024.core.util.Nick;
import pl.kamil0024.core.util.UserUtil;

import javax.annotation.Nonnull;

public class MultiListener extends ListenerAdapter {

    private final MultiDao multiDao;

    public MultiListener(MultiDao multiDao) {
        this.multiDao = multiDao;
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (!event.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }

            Member mem = event.getGuild().retrieveMemberById(event.getMember().getId()).complete();
            String mc = UserUtil.getMcNick(mem, true);

            MultiConfig conf = multiDao.get(event.getUser().getId());
            conf.getNicki().add(new Nick(mc, new BDate().getTimestamp()));
            multiDao.save(conf);
        }).start();

    }

}
