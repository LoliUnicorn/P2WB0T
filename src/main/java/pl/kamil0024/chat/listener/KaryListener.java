package pl.kamil0024.chat.listener;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.chat.Action;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.moderation.MuteCommand;
import pl.kamil0024.commands.moderation.PunishCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.stats.StatsModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class KaryListener extends ListenerAdapter {

    private final KaryJSON karyJSON;
    private final CaseDao caseDao;
    private final ModLog modLog;
    private final StatsModule statsModule;

    @Getter private static final ArrayList<Action> embedy = new ArrayList<>();

    public KaryListener(KaryJSON karyJSON, CaseDao caseDao, ModLog modLog, StatsModule statsModule) {
        this.karyJSON = karyJSON;
        this.caseDao = caseDao;
        this.modLog = modLog;
        this.statsModule = statsModule;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (!event.getChannel().getId().equals(Ustawienia.instance.channel.moddc)) return;
        if (UserUtil.getPermLevel(event.getMember()).getNumer() == PermLevel.MEMBER.getNumer()) return;
        if (event.getMember().getUser().isBot()) return;


        check(event);
    }

    private synchronized void check(GuildMessageReactionAddEvent event) {
        try {
            for (Action entry : getEmbedy()) {
                if (!entry.getBotMsg().equals(event.getMessageId())) continue;

                Message msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
                if (event.getReactionEmote().getId().equals(Ustawienia.instance.emote.red)) {
                    msg.delete().complete();
                    getEmbedy().remove(entry);
                    return;
                }

                Member mem = event.getGuild().retrieveMemberById(entry.getMsg().getAuthor().getId()).complete();
                if (mem == null) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", użytkownik wyszedł z serwera??")
                            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    continue;
                }
                if (MuteCommand.hasMute(mem)) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", użytkownik jest wyciszony!")
                            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    continue;
                }

                KaryJSON.Kara kara = karyJSON.getByName(entry.getKara().getPowod());
                if (kara == null) {
                    event.getChannel().sendMessage(event.getMember().getAsMention() + ", kara `" + entry.getKara().getPowod() + "` jest źle wpisana!").queue();
                    continue;
                }

                PunishCommand.putPun(kara, Collections.singletonList(mem), event.getMember(), event.getChannel(), caseDao, modLog, statsModule);
                getEmbedy().remove(entry);
                msg.delete().queue();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
