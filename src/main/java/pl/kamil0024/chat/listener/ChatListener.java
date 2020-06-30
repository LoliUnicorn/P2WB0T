package pl.kamil0024.chat.listener;

import com.google.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.chat.Action;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.moderation.MuteCommand;
import pl.kamil0024.commands.moderation.PunishCommand;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.Emoji;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.KaryJSON;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.core.JsonPointer.SEPARATOR;

public class ChatListener extends ListenerAdapter {

    private static final File FILE = new File("res/przeklenstwa.api");
    private static final String HTTPS = "\\w+:\\/{2}[\\d\\w-]+(\\.[\\d\\w-]+)*(?:(?:[^\\s/]*))*";
    private static final String HTTP = "([0-9a-z_-]+\\.)+(com|infonet|net|org|pro|de|ggmc|md|me|tt|tv|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt)";
    private static final String DISCORD_INVITE = "(https?://)?(www\\.)?(discord\\.(gg|io|me|li)|discordapp\\.com/invite)/.+[a-z]";

    private static final Pattern EMOJI = Pattern.compile("<(a?):(\\w{2,32}):(\\d{17,19})>");

    @Inject private JDA api;
    @Inject private KaryJSON karyJSON;
    @Inject private CaseDao caseDao;
    @Inject private ModLog modLog;

    List<String> przeklenstwa;

    public ChatListener(JDA api, KaryJSON karyJSON, CaseDao caseDao, ModLog modLog) {
        this.api = api;
        this.karyJSON = karyJSON;
        this.modLog = modLog;
        this.caseDao = caseDao;
        this.przeklenstwa = new ArrayList<>();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        if (UserUtil.getPermLevel(e.getAuthor()).getNumer() >= PermLevel.HELPER.getNumer()) return;
        if (e.getAuthor().isBot() || e.getAuthor().isFake() || e.isWebhookMessage() || e.getMessage().getContentRaw().isEmpty()) return;
        if (e.getChannel().getId().equals("426809411378479105") || e.getChannel().getId().equals("503294063064121374")) return;
        checkMessage(e.getMember(), e.getMessage(), karyJSON, caseDao, modLog);
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent e) {
        if (UserUtil.getPermLevel(e.getAuthor()).getNumer() >= PermLevel.HELPER.getNumer()) return;
        if (e.getAuthor().isBot() || e.getAuthor().isFake() || e.getMessage().getContentRaw().isEmpty()) return;
        if (e.getChannel().getId().equals("426809411378479105") || e.getChannel().getId().equals("503294063064121374")) return;
        checkMessage(e.getMember(), e.getMessage(), karyJSON, caseDao, modLog);
    }

    public static void checkMessage(Member member, Message msg, KaryJSON karyJSON, CaseDao caseDao, ModLog modLog) {
        synchronized (msg.getAuthor().getId()) {
            if (MuteCommand.hasMute(member)) return;

            String msgRaw = msg.getContentRaw();
            Action action = new Action(karyJSON);
            action.setMsg(msg);

            String przeklenstwa = msgRaw.replaceAll("[^\\w\\s]*", "");
            String[] tak = new String[] {"a;ą", "c;ć","e;ę", "l;ł", "n;ń", "o;ó", "s;ś", "z;ź", "z;ż"};

            for (String s : tak) {
                String[] kurwa = s.split(";");
                przeklenstwa = przeklenstwa.replaceAll(kurwa[1], kurwa[0]);
            }

            if (containsSwear(przeklenstwa.split(" ")) != null) {
                msg.delete().queue();
//                msg.getChannel().sendMessage(String.format("<@%s>, ładnie to tak przeklinać?", msg.getAuthor().getId())).queue();

                KaryJSON.Kara kara = karyJSON.getByName("Wszelkiej maści wyzwiska, obraza, wulgaryzmy, prowokacje, groźby i inne formy przemocy");
                if (kara == null) {
                    Log.newError("Powod przy nadawaniu kary za przeklenstwa jest nullem");
                } else {
                    PunishCommand.putPun(kara,
                            Collections.singletonList(member),
                            member.getGuild().getSelfMember(),
                            msg.getTextChannel(),
                            caseDao, modLog);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
                return;
            }

            if (containsLink(msgRaw.split(" ")) != null) {
                action.setKara(Action.ListaKar.LINK);
                action.send();
                return;
            }

            if (containsInvite(msgRaw.split(" "))) {
                msg.delete().queue();
                action.setKara(Action.ListaKar.LINK);
                action.send();
                return;
            }

            int flood = containsFlood(msgRaw.replaceAll("<@!?([0-9])*>", "").replaceAll("[^\\w\\s]*", ""));
            if (flood > 4 || containsCaps(msgRaw.replaceAll("[^\\w\\s]*", "")) > 5 || emoteCount(msg.getContentRaw(), msg.getJDA()) > 3) {
                msg.delete().queue();
                action.setKara(Action.ListaKar.FLOOD);
                action.send();
            }

            // Może to nie być w 100% prawdziwe
            if (containsSwear(new String[] {msg.getContentRaw()}) != null) {
                action.setKara(Action.ListaKar.ZACHOWANIE);
                action.send();
            }
        }
    }

    @Nullable
    public static String containsSwear(String[] list) {
        File file = FILE;
        if (!file.exists()) {
            Log.error("Plik do przeklenstw nie istnieje! FIle=" + file);
            return null;
        }
        List<String> przeklenstwa = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) { przeklenstwa.add(line); }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        for (String s : list) {
            if (s != null && !s.isEmpty()) {
                if (przeklenstwa.contains(s.toLowerCase())) return s.toLowerCase();
            }
        }
        return null;
    }

    @SuppressWarnings("DuplicatedCode")
    @Nullable
    public static String containsLink(String[] list) {
        for (String s : list) {
            if (s != null && !s.isEmpty() && !s.toLowerCase().contains("gram na")) {
                s = s.toLowerCase().replaceAll("derpmc.pl", "tak")
                        .replaceAll("feerko.pl", "tak")
                        .replaceAll("hajsmc.pl", "tak")
                        .replaceAll("roizy.pl", "tak")
                        .replaceAll("hypixel\\.net", "tak")
                        .replaceAll("blazingpack\\.pl", "tak");
                String xd = s.replaceAll(HTTP, "CzemuTutajJestJakisJebanyInvite");
                String xdd = s.replaceAll(HTTPS, "CzemuTutajJestJakisJebanyInvite");
                if (xd.equals("CzemuTutajJestJakisJebanyInvite") || xdd.equals("CzemuTutajJestJakisJebanyInvite")) {
                    return s;
                }
            }
        }
        return null;
    }

    public static boolean containsInvite(String[] list) {
        for (String s : list) {
            String tak = s.replaceAll(DISCORD_INVITE, "CzemuTutajJestJakisJebanyInvite");
            if (tak.contains("CzemuTutajJestJakisJebanyInvite")) return true;
        }
        return false;
    }

    public static int containsFlood(String msg) {
        if (msg.length() < 3) return 0;
        msg = msg.replaceAll(" ", "");

        int tak = 0;
        int flood = 0;
        String[] ssplit = msg.split("");
        for (String split : ssplit) {
            try {
                String nastepnaLitera = ssplit[tak + 1];
                if (!split.equals("") && !nastepnaLitera.equals("") && split.equals(nastepnaLitera)) flood++;
                tak++;
            } catch (Exception ignored) {}
        }

        return flood;
    }

    public static int containsCaps(String msg) {
        msg = msg.replaceAll(" ", "").replaceAll("<@!?([0-9])*>", "");

        int caps = 0;
        char[] split = msg.toCharArray();
        if (split.length < 5) return 0;

        for (char s : split) {
            assert !String.valueOf(s).equals("");
            if (Character.isUpperCase(s)) caps++;
        }

        return caps;
    }

    public static int emoteCount(String msg, JDA api) {
        int count = 0;
        Matcher m = EMOJI.matcher(msg);
        while (m.find())
            count++;
        for (String s : msg.split(" ")) {
            Emoji tak = Emoji.resolve(s, api);
            if (tak != null) {
                count++;
            }
        }
        return count;
    }


}
