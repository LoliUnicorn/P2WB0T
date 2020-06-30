package pl.kamil0024.chat;

import lombok.Data;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import pl.kamil0024.chat.listener.KaryListener;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.KaryJSON;

import java.awt.*;
import java.util.Objects;

@Data
public class Action {

    private ListaKar kara;
    private Message msg;
    private KaryJSON karyJSON;

    public Action(KaryJSON karyJSON) {
        this.karyJSON = karyJSON;
    }

    public Action(ListaKar kara, Message msg, KaryJSON karyJSON) {
        this.kara = kara;
        this.msg = msg;
    }

    @SuppressWarnings("ConstantConditions")
    public void send() {
        if (kara == null || msg == null) throw new NullPointerException("kara lub msg jest nullem");

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.red);

        eb.addField("Użytkownik", UserUtil.getFullNameMc(Objects.requireNonNull(msg.getMember())), false);
        eb.addField("Treść wiadomości", msg.getContentRaw(), false);
        eb.addField("Kanał", msg.getTextChannel().getAsMention(), false);
        eb.addField("Za co ukarać?", kara.getPowod(), false);

        TextChannel txt = msg.getJDA().getTextChannelById(Ustawienia.instance.channel.moddc);
        if (txt == null) throw new NullPointerException("Kanał do modów dc jest nullem");
        txt.sendMessage(eb.build()).queue(m -> {
                m.addReaction(CommandExecute.getReaction(msg.getAuthor(), true)).queue();
                m.addReaction(CommandExecute.getReaction(msg.getAuthor(), false)).queue();
                KaryListener.getEmbedy().put(m.getId() + "-" + msg.getMember().getId(), getKara());
        });
    }

    public enum ListaKar {
        ZACHOWANIE("Wszelkiej maści wyzwiska, obraza, wulgaryzmy, prowokacje, groźby i inne formy przemocy"),
        FLOOD("Nadmierny spam, flood lub caps lock wiadomościami lub emotikonami"),
        LINK("Reklama stron, serwisów lub serwerów gier/Discord niepowiązanych w żaden sposób z P2W.pl");

        @Getter private final String powod;

        ListaKar(String powod) {
            this.powod = powod;
        }

    }

}
