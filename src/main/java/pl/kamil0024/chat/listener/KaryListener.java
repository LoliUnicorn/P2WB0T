package pl.kamil0024.chat.listener;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.chat.Action;
import pl.kamil0024.commands.CommandsModule;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.moderation.PunishCommand;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.KaryJSON;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KaryListener extends ListenerAdapter {

    private final KaryJSON karyJSON;
    private final CaseDao caseDao;
    private final ModLog modLog;

    @Getter private static final HashMap<String, Action.ListaKar> embedy = new HashMap<>();

    public KaryListener(KaryJSON karyJSON, CaseDao caseDao, ModLog modLog) {
        this.karyJSON = karyJSON;
        this.caseDao = caseDao;
        this.modLog = modLog;
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (!event.getChannel().getId().equals(Ustawienia.instance.channel.moddc)) return;
        if (UserUtil.getPermLevel(event.getMember()).getNumer() == PermLevel.MEMBER.getNumer()) return;

        try {
            for (Map.Entry<String, Action.ListaKar> entry : getEmbedy().entrySet()) {
                String[] tak = entry.getKey().split("-");
                if (!tak[0].equals(event.getMessageId())) continue;
                Member mem = event.getGuild().retrieveMemberById(tak[1]).complete();
                if (mem == null) {
                    event.getChannel().sendMessage("Użytkownik wyszedł z serwera??").queue();
                    continue;
                }
                Message msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
                if (event.getReactionEmote().getId().equals(Ustawienia.instance.emote.red)) {
                    getEmbedy().remove(entry.getKey());
                    msg.delete().queue();
                    return;
                }
                KaryJSON.Kara kara = karyJSON.getByName(entry.getValue().getPowod());
                if (kara == null) {
                    event.getChannel().sendMessage("Kara `" + entry.getValue().getPowod() + "` jest źle wpisana!").queue();
                    continue;
                }
                PunishCommand.putPun(kara, Collections.singletonList(mem), event.getMember(), event.getChannel(), caseDao, modLog);
                getEmbedy().remove(entry.getKey());
                msg.delete().queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
