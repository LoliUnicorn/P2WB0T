package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.UserUtil;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;

public class UserinfoCommand extends Command {

    public UserinfoCommand() {
        name = "userinfo";
        aliases.add("infouser");
        cooldown = 60;
    }

    @Override
    public boolean execute(CommandContext context) {
        SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy `@` HH:mm:ss");
        EmbedBuilder eb = new EmbedBuilder();

        User user = context.getUser();
        User userArg = context.getParsed().getUser(context.getArgs().get(0));
        if (userArg != null) user = userArg;

        Member member = null;
        try {
            member = context.getGuild().retrieveMemberById(user.getId()).complete();;
        } catch (ErrorResponseException ignored) {}

        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setFooter("Userinfo");
        eb.setTimestamp(Instant.now());
        eb.setThumbnail(user.getAvatarUrl());

        eb.addField("Nick", user.getAsMention() + " [" + UserUtil.getMcNick(member) + "]", false);

        long lonk = context.getUser().getTimeCreated().toInstant().toEpochMilli();
        long date = new BDate().getTimestamp();

        Date discord = new Date(user.getTimeCreated().toInstant().toEpochMilli());
        eb.addField(context.getTranslate("userinfo.dcjoin"), sfd.format(discord), false); // + " `" + new BDate(date, ModLog.getLang()).difference(lonk) + "` temu"

        if (member != null) {
            Date serwer = new Date(member.getTimeJoined().toInstant().toEpochMilli());
            long lonk2 = member.getTimeJoined().toInstant().toEpochMilli();
            eb.addField(context.getTranslate("userinfo.serverjoin"), sfd.format(serwer), false); // + " `" + new BDate(lonk2, ModLog.getLang()).difference(date) + "` temu"
            eb.addField(context.getTranslate("userinfo.status"), translateStatus(member.getOnlineStatus()), false);
            try {
                eb.addField(context.getTranslate("userinfo.game"), member.getActivities().get(0).getName(), false);
            } catch (Exception ignored) {}
        }
        PermLevel pm = UserUtil.getPermLevel(user);
        eb.addField(context.getTranslate("userinfo.permlvl"), context.getTranslate(pm.getTranlsateKey()) + " (" + pm.getNumer() + ")", false);
        if (!user.getFlags().isEmpty()) {
            eb.addField(context.getTranslate("userinfo.bagnes"), formatFlags(user.getFlags(), context.getJDA()), false);
        }
        context.send(eb.build()).queue();
        return true;
    }

    private String formatFlags(EnumSet<User.UserFlag> flags, JDA jda) {
        StringBuilder sb = new StringBuilder();
        Emote green = Objects.requireNonNull(jda.getGuildById(Ustawienia.instance.bot.guildId)).retrieveEmoteById(Ustawienia.instance.emote.green).complete();
        for (User.UserFlag value : User.UserFlag.values()) {
            if (flags.contains(value)) {
                sb.append(green.getAsMention()).append(" ").append(value.getName()).append("\n");
            }
        }
        return sb.toString();
    }

    private String translateStatus(OnlineStatus onlineStatus) {
        switch (onlineStatus) {
            case ONLINE:
                return "Online";
            case IDLE:
                return "Zaraz wrazam";
            case DO_NOT_DISTURB:
                return "Nie przeszkadzać";
            case INVISIBLE:
                return "Niewidzialny lol";
            case OFFLINE:
                return "Offline";
            case UNKNOWN:
                return "Unknow";
            default:
                return "???";
        }
    }

}
