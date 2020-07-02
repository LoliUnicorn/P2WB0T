package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.core.util.UserUtil;

import java.util.Collections;

public class CytujCommand extends Command {

    public CytujCommand() {
        name = "cytuj";
        permLevel = PermLevel.HELPER;
        cooldown = 30;
    }

    @Override
    public boolean execute(CommandContext context) {
        String msgId = context.getArgs().get(0);
        String komentarz = context.getArgs().get(1);
        if (msgId == null) throw new UsageException();

        Message msg = null;

        if (!msgId.contains("-")) {
            msg = kurwaJDA(context.getChannel(), msgId);
        } else {
            String[] wybierajSe = msgId.split("-");
            TextChannel txt;
            txt = context.getGuild().getTextChannelById(wybierajSe[0]);
            if (txt != null) msg = kurwaJDA(txt, wybierajSe[1]);
            else {
                txt = context.getGuild().getTextChannelById(wybierajSe[1]);
                if (txt != null) msg = kurwaJDA(txt, wybierajSe[0]);
            }
        }

        if (msg == null) {
            context.send("Złe ID wiadomości!").queue();
            return false;
        }

        if (msg.getContentRaw().length() >= MessageEmbed.VALUE_MAX_LENGTH) {
            context.send("Treść wiadomości jest za długa!").queue();
            return false;
        }
        String takEmbed = msg.getEmbeds().isEmpty() ? "" : "Zawiera embed, patrz poniżej";

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setFooter("Cytuj");
        eb.setAuthor(UserUtil.getName(msg.getAuthor()), null, msg.getAuthor().getAvatarUrl());
        eb.setDescription(msg.getContentRaw().isEmpty() ? " Wiadomość pusta. " + takEmbed : msg.getContentRaw());
        eb.addField("Kolega prosi o linka?", String.format("[%s](%s)", "KLIK", msg.getJumpUrl()), false);
        eb.setTimestamp(msg.getTimeCreated());

        MessageBuilder mb = new MessageBuilder();
        if (komentarz != null) mb.setContent("**" + UserUtil.getMcNick(context.getMember(), true) + "**: " + komentarz);
        mb.setEmbed(eb.build());
        context.getChannel().sendMessage(mb.build()).
                allowedMentions(Collections.singleton(Message.MentionType.EMOTE)).
                complete();
        if (!takEmbed.isEmpty()) context.send(msg.getEmbeds().get(0)).queue();
        return true;
    }

    @Nullable
    public static Message kurwaJDA(TextChannel txt, String msgId) {
        try {
            return txt.retrieveMessageById(msgId).complete();
        } catch (ErrorResponseException ignored) { }
        return null;
    }
    
}
