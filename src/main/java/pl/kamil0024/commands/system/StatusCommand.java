package pl.kamil0024.commands.system;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.BetterStringBuilder;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;

import java.util.concurrent.TimeUnit;

public class StatusCommand extends Command {

    private EventWaiter eventWaiter;

    public StatusCommand(EventWaiter eventWaiter) {
        name = "status";
        permLevel = PermLevel.ADMINISTRATOR;

        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgs().get(0) == null) throw new UsageException();
        TextChannel txt = context.getGuild().getTextChannelById(Ustawienia.instance.channel.status);
        if (txt == null) throw new NullPointerException("Kanal do statusu jest nullem");

        if (!txt.canTalk()) {
            context.send("Nie mam permisji do pisania na " + txt.getAsMention() + "!").queue();
            return false;
        }

        Boolean feerko = null, roizy = null, derp = null;
        String[] serwery = context.getArgs().get(0).split(",");
        for (String s : serwery) {
            String tak = s.toLowerCase();
            if (tak.equals("feerko")) feerko = true;
            if (tak.equals("roizy")) roizy = true;
            if (tak.equals("derpmc")) derp = true;
        }

        if (feerko == null && roizy == null && derp == null) {
            context.send("Wpisałeś złe nazwy serwerów!").queue();
            return false;
        }

        Message botMsg = null;
        MessageHistory history = txt.getHistoryFromBeginning(15).complete();
        if (history.isEmpty()) {
            botMsg = txt.sendMessage(getMsg(Emote.ONLINE, Emote.ONLINE, Emote.ONLINE, null)).complete();
        }

        if (botMsg == null) {
            for (Message message : history.getRetrievedHistory()) {
                assert message.getAuthor().getId().equals(context.getBot().getId());
                botMsg = message;
                break;
            }
        }
        if (botMsg == null) throw new NullPointerException("Nie udało się znaleźć wiadomości bota");

        StringBuilder sb = new StringBuilder();
        if (derp != null) sb.append("derpmc, ");
        if (feerko != null) sb.append("feerko, ");
        if (roizy != null) sb.append("roizy, ");

        final Message waitMsg = context.send("Na jaki status chcesz zmienić serwer(-y) " + sb.toString() + " ?").complete();
        waitMsg.addReaction(Emote.ONLINE.getUnicode()).queue();
        waitMsg.addReaction(Emote.WARN.getUnicode()).queue();
        waitMsg.addReaction(Emote.OFF.getUnicode()).queue();
        waitMsg.addReaction(Emote.RESTART.getUnicode()).queue();

        Boolean finalDerp = derp;
        Boolean finalRoizy = roizy;
        Boolean finalFeerko = feerko;
        Message finalBotMsg = botMsg;
        eventWaiter.waitForEvent(GuildMessageReactionAddEvent.class,
                (event) -> event.getUser().getId().equals(context.getUser().getId()) && event.getMessageId().equals(waitMsg.getId()),
                (event) -> {
                    waitMsg.clearReactions().queue();
                    if (event.getReactionEmote().isEmote()) return;

                    Emote e = Emote.byUnicode(event.getReaction().getReactionEmote().getEmoji());
                    if (e == null) return;

                    finalBotMsg.editMessage(getMsg(finalDerp == null ? null : e,
                            finalFeerko == null ? null : e,
                            finalRoizy == null ? null : e, finalBotMsg)).queue();

                    waitMsg.editMessage("Pomyślnie zmieniono!").queue();
                    waitMsg.clearReactions().queue();
                },30, TimeUnit.SECONDS, () -> waitMsg.clearReactions().queue());

        return true;
    }

    @Getter
    private enum Emote {
        ONLINE("\u2705"),
        WARN("\u26A0"),
        OFF("\uD83D\uDED1"),
        RESTART("\uD83D\uDD04");

        public String unicode;

        Emote(String unicode) {
            this.unicode = unicode;
        }

        @Nullable
        public static Emote byUnicode(String uni) {
            for (Emote value : Emote.values()) {
                if (value.getUnicode().equals(uni)) return value;
            }
            return null;
        }

    }

    private String getMsg(@Nullable Emote derp, @Nullable Emote feerko, @Nullable Emote roizy, @Nullable Message botMsg) {
        String xd = "\uD83D\uDD36";
        BetterStringBuilder sb = new BetterStringBuilder();

        if (botMsg == null) {
            if (derp == null) derp = Emote.ONLINE;
            if (feerko == null) feerko = Emote.ONLINE;
            if (roizy == null) roizy = Emote.ONLINE;
        } else {
            for (String s : botMsg.getContentRaw().split("\n")) {
                if (!s.isEmpty() && s.contains("-> ")) {
                    String emote = s.split("-> ")[1];
                    if (derp == null) if (s.contains("DerpMC.PL")) derp = Emote.byUnicode(emote);
                    if (feerko == null) if (s.contains("Feerko.PL")) feerko = Emote.byUnicode(emote);
                    if (roizy == null) if (s.contains("RoiZy.PL")) roizy = Emote.byUnicode(emote);
                }
            }
        }

        sb.appendLine(xd + " STATUS SERWERÓW" + xd);
        sb.appendLine("Na tym kanale możecie sprawdzić status serwerów. Status jest aktualizowany ręcznie przez nas, więc tutaj możecie się dowiedzieć, czy jesteśmy świadomi sytuacji i jakie kroki już podejmujemy.\n");

        sb.appendLine("DerpMC.PL -> " + derp.getUnicode());
        sb.appendLine("Feerko.PL -> " + feerko.getUnicode());
        sb.appendLine("RoiZy.PL -> " + roizy.getUnicode() + "\n");

        sb.appendLine("Legenda:");
        sb.appendLine(Emote.ONLINE.getUnicode() + " Serwer chodzi sprawnie, bądź nie wiemy o problemie");
        sb.appendLine(Emote.WARN.getUnicode() + " Występują problemy na serwerze, sprawdzamy przyczynę");
        sb.appendLine(Emote.OFF.getUnicode() + " Awaria serwera");
        sb.appendLine(Emote.RESTART.getUnicode() + " Serwer jest restartowany");

        return sb.toString();
    }

}
