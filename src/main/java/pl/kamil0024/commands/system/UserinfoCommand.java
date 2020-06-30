package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.Duration;
import pl.kamil0024.core.util.UserUtil;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

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
        if (UserUtil.getPermLevel(user).getNumer() >= PermLevel.HELPER.getNumer() && context.getParsed().getUser(context.getArgs().get(0)) != null) {
            user = context.getParsed().getUser(context.getArgs().get(0));
        }

        if (user == null) user = context.getUser(); // super jestes idea
        Member member = context.getGuild().getMember(user);

        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setFooter("Userinfo");
        eb.setTimestamp(Instant.now());
        eb.setThumbnail(user.getAvatarUrl());

        eb.addField("Nick", user.getAsMention() + " [" + UserUtil.getMcNick(member) + "]", false);

        long lonk = context.getUser().getTimeCreated().toInstant().toEpochMilli();
        long date = new BDate().getTimestamp();

        Date discord = new Date(user.getTimeCreated().toInstant().toEpochMilli());
        eb.addField("Dołączył na Discorda", sfd.format(discord) + " `" + new BDate(lonk, ModLog.getLang()).difference(date) + "` temu", false);

        if (member != null) {
            Date serwer = new Date(member.getTimeJoined().toInstant().toEpochMilli());
            long lonk2 = member.getTimeJoined().toInstant().toEpochMilli();
            eb.addField("Dołączył na Serwer", sfd.format(serwer) + " `" + new BDate(lonk2, ModLog.getLang()).difference(date) + "` temu", false);
            eb.addField("Status", translateStatus(member.getOnlineStatus()), false);
            try {
                eb.addField("Gra w", member.getActivities().get(0).getName(), false);
            } catch (Exception ignored) {}
        }
        PermLevel pm = UserUtil.getPermLevel(user);
        eb.addField("Poziom uprawnień", context.getTranslate(pm.getTranlsateKey()) + " (" + pm.getNumer() + ")", false);
        context.send(eb.build()).queue();
        return true;
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
