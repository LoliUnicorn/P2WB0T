/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.chat.listener;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.kamil0024.chat.Action;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.moderation.MuteCommand;
import pl.kamil0024.commands.moderation.PunishCommand;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.Emoji;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Dowod;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.stats.StatsModule;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;


import static java.nio.charset.StandardCharsets.*;

@SuppressWarnings("DuplicatedCode")
public class ChatListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChatListener.class);

    private static final Pattern HTTP = Pattern.compile("([0-9a-z_-]+\\.)+(com|infonet|net|org|pro|de|ggmc|md|me|tt|tv|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt)");
    private static final String DISCORD_INVITE = "(https?://)?(www\\.)?(discord\\.(gg|io|me|li)|discordapp\\.com/invite)/.+[a-z]";

    private static final Pattern EMOJI = Pattern.compile("<(a?):(\\w{2,32}):(\\d{17,19})>");

    @Inject private final KaryJSON karyJSON;
    @Inject private final CaseDao caseDao;
    @Inject private final ModLog modLog;
    @Inject private final StatsModule statsModule;

    @Getter private final List<String> przeklenstwa;

    public ChatListener(ShardManager api, KaryJSON karyJSON, CaseDao caseDao, ModLog modLog, StatsModule statsModule) {
        this.karyJSON = karyJSON;
        this.modLog = modLog;
        this.caseDao = caseDao;
        this.statsModule = statsModule;

        InputStream res = Main.class.getClassLoader().getResourceAsStream("przeklenstwa.api");
        if (res == null) {
            Log.newError("Plik przeklenstwa.api jest nullem", ChatListener.class);
            throw new NullPointerException("Plik przeklenstwa.api jest nullem");
        }

        this.przeklenstwa = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res, UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) { przeklenstwa.add(line); }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (przeklenstwa.isEmpty()) Log.newError("Lista przeklenstw jest nullem!", ChatListener.class);
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        if (!e.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;
        if (e.getChannel().getParent() != null && e.getChannel().getParent().getId().equals("539819570358386698")) return;
        if (UserUtil.getPermLevel(e.getAuthor()).getNumer() >= PermLevel.CHATMOD.getNumer()) return;
        if (e.getAuthor().isBot() || e.getAuthor().isFake() || e.getMessage().getContentRaw().isEmpty()) return;
        if (e.getChannel().getId().equals("426809411378479105") || e.getChannel().getId().equals("503294063064121374") || e.getChannel().getId().equals("573873102757429256")) return;
        if (e.getChannel().getId().equals("426864003562864641") &&
                !e.getMessage().getContentRaw().isEmpty() && e.getMessage().getContentRaw().length() >= 2 ||
                e.getMessage().getContentRaw().toCharArray()[1] == 'p') {
            return;
        }
        checkMessage(e.getMember(), e.getMessage(), karyJSON, caseDao, modLog);
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent e) {
        if (!e.getGuild().getId().equals(Ustawienia.instance.bot.guildId)) return;
        if (UserUtil.getPermLevel(e.getAuthor()).getNumer() >= PermLevel.CHATMOD.getNumer()) return;
        if (e.getAuthor().isBot() || e.getAuthor().isFake() || e.getMessage().getContentRaw().isEmpty()) return;
        if (e.getChannel().getId().equals("426809411378479105") || e.getChannel().getId().equals("503294063064121374") || e.getChannel().getId().equals("573873102757429256")) return;
        checkMessage(e.getMember(), e.getMessage(), karyJSON, caseDao, modLog);
    }

    public void checkMessage(Member member, Message msg, KaryJSON karyJSON, CaseDao caseDao, ModLog modLog) {
        if (MuteCommand.hasMute(member)) return;

        String czystaWiadomosc = msg.getContentRaw();
        String[] split = czystaWiadomosc.split("\n");
        if (czystaWiadomosc.startsWith("> ") && split.length >= 1) {
            try {
                czystaWiadomosc = czystaWiadomosc.replaceAll(split[0], "");
            } catch (Exception ignored) { }
        }

        String msgRaw = czystaWiadomosc.replaceAll("<@!?([0-9])*>", "")
                .replaceAll("3", "e")
                .replaceAll("1", "i")
                .replaceAll("0", "o")
                .replaceAll("v", "u")
                .replaceAll("<#(\\d+)>", "");
        Action action = new Action(karyJSON);
        action.setMsg(msg);

        String przeklenstwa = msgRaw;

        String[] tak = new String[] {"a;ą", "c;ć","e;ę", "l;ł", "n;ń", "o;ó", "s;ś", "z;ź", "z;ż"};
        for (String s : tak) {
            String[] kurwa = s.split(";");
            przeklenstwa = przeklenstwa.replaceAll(kurwa[1], kurwa[0]);
        }
        przeklenstwa = przeklenstwa.replaceAll("[^\\u0020\\u0030-\\u0039\\u0041-\\u005A\\u0061-\\u007A\\u00C0-\\u1D99]", "");

        if (containsSwear(przeklenstwa.split(" ")) != null) {
            msg.delete().queue();
//                msg.getChannel().sendMessage(String.format("<@%s>, ładnie to tak przeklinać?", msg.getAuthor().getId())).queue();

            KaryJSON.Kara kara = karyJSON.getByName("Wszelkiej maści wyzwiska, obraza, wulgaryzmy, prowokacje, groźby i inne formy przemocy");
            Dowod d = new Dowod(1, msg.getGuild().getSelfMember().getId(), msg.getContentDisplay(), null);
            if (kara == null) {
                Log.newError("Powod przy nadawaniu kary za przeklenstwa jest nullem", ChatListener.class);
            } else {
                PunishCommand.putPun(kara,
                        Collections.singletonList(member),
                        member.getGuild().getSelfMember(),
                        msg.getTextChannel(),
                        caseDao, modLog, statsModule, d, null);
                return;
            }
        }

        if (containsLink(msgRaw.split(" ")) && !msg.getChannel().getId().equals("426864003562864641")) {
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

        if (msgRaw.replaceAll("(http(s)?://)?(www\\.)?(m\\.)?(youtube\\.com|youtu\\.be)/\\S+", "kurwa").contains("kurwa")) {
            if (!msg.getChannel().getId().equals("426864003562864641")) {
                Role miniyt = member.getGuild().getRoleById("425670776272715776");
                Role yt = member.getGuild().getRoleById("425670600049295360");
                if (miniyt == null || yt == null) {
                    Log.newError("Rola miniyt/yt jest nullem", ChatListener.class);
                    return;
                }
                if (!member.getRoles().contains(miniyt) || !member.getRoles().contains(yt)) {
                    msg.delete().queue();
                    action.setKara(Action.ListaKar.LINK);
                    action.send();
                }
            }

        }

        String takMsg = czystaWiadomosc.replaceAll("<@(&?)(!?)([0-9])*>", "")
                .replaceAll("<#(\\d+)>", "");

        int emote = emoteCount(takMsg, msg.getJDA());

        String bezEmotek = takMsg.replaceAll(EMOJI.toString(), "");
        String capsMsg = bezEmotek.replaceAll("[^\\w\\s]*", "");
        int caps = containsCaps(capsMsg);

        int flood = containsFlood(bezEmotek);

        if (!msg.getChannel().getId().equals("652927860943880224")) {
            if (caps >= 50 || emote >= 10) {
                logger.debug("---------------------------");
                logger.debug("user: " + msg.getAuthor().getId());
                logger.debug("msg: " + takMsg);
                logger.debug("int flooda: " + flood);
                logger.debug("procent capsa " + caps);
                logger.debug("int emotek: " + emote);
                logger.debug("---------------------------");
                msg.delete().queue();
                action.setKara(Action.ListaKar.FLOOD);
                action.send();
                return;
            }
            if (flood >= 10) {
                action.setPewnosc(false);
                action.setDeleted(false);
                action.setKara(Action.ListaKar.FLOOD);
                action.send();
                return;
            }
        }
        
        if (msg.getChannel().getId().equals("739975462247202816")) {
            if (containsTestFlood(bezEmotek) == 100) {
                msg.delete().queue();
                action.setKara(Action.ListaKar.FLOOD);
                action.send();
                return;
            }
        }

        // Może to nie być w 100% prawdziwe
        action.setPewnosc(false);
        action.setDeleted(false);

//        if (skrotyCount(takMsg.toLowerCase().split(" "))) {
//            action.setKara(Action.ListaKar.SKROTY);
//            action.send();
//            return;
//        }
//        if (skrotyCount(new String[] {takMsg.toLowerCase()})) {
//            action.setKara(Action.ListaKar.SKROTY);
//            action.send();
//        }

        for (String s : getPrzeklenstwa()) {
            if (przeklenstwa.toLowerCase().contains(s) || przeklenstwa.replaceAll(" ", "").toLowerCase().contains(s)) {
                action.setKara(Action.ListaKar.ZACHOWANIE);
                action.send();
                return;
            }
        }

    }

    @Nullable
    public String containsSwear(String[] list) {
        for (String s : list) {
            if (s != null && !s.isEmpty()) {
                if (getPrzeklenstwa().contains(s.toLowerCase())) return s.toLowerCase();
            }
        }
        return null;
    }

    public static boolean containsLink(String[] list) {
        for (String s : list) {
            if (s.contains("derpmc") || s.contains("roizy") || s.contains("p2w") || s.contains("hypixel") || s.contains("discord")) continue;
            try {
                new URL(s);
                return true;
            } catch (MalformedURLException e) {
                Matcher mat = HTTP.matcher(s);
                if (mat.matches()) return true;
            }
        }
        return false;
    }

    public static boolean containsInvite(String[] list) {
        for (String s : list) {
            String tak = s.replaceAll(DISCORD_INVITE, "CzemuTutajJestJakisJebanyInvite");
            if (tak.contains("CzemuTutajJestJakisJebanyInvite")) return true;
        }
        return false;
    }


    public static double containsTestFlood(String msg) {
        if (msg.length() <= 4 || msg.toLowerCase().contains("zaraz")) return 0;
        HashMap<Character, Integer> mapa = new HashMap<>();

        for (char c : msg.replaceAll(" ", "").toCharArray()) {
            Integer ind = mapa.getOrDefault(c, 0);
            ind++;
            mapa.put(c, ind);
        }

        int suma = 0;
        for (Map.Entry<Character, Integer> entry : mapa.entrySet()) {
            if (entry.getValue() > 1) {
                suma += entry.getValue();
            }
        }
        return ((double) suma / (double) msg.length()) * 100;
    }

    public static int containsFlood(String msg) {
        if (msg.length() < 3 || containsLink(new String[] {msg})) return 0;

        int tak = 0;
        int flood = 0;
        String[] ssplit = msg.split("");
        String floodowanyZnak = null;
        for (String split : ssplit) {
            try {
                if (split.equals(" ")) continue;
                String nastepnaLitera = ssplit[tak + 1];
                if (floodowanyZnak == null && !split.equals("") && !nastepnaLitera.isEmpty() && split.toLowerCase().equals(nastepnaLitera.toLowerCase())) {
                    floodowanyZnak = nastepnaLitera;
                    flood++;
                } else if (floodowanyZnak != null && floodowanyZnak.toLowerCase().equals(split.toLowerCase())) {
                    flood++;
                } else {
                    floodowanyZnak = nastepnaLitera;
                    if (flood < 10) flood = 0;
                }
                tak++;
            } catch (Exception ignored) {}
        }
        return flood;
    }

    public static int containsCaps(String msg) {
        msg = msg.replaceAll(" ", "")
                .replaceAll("<@!?([0-9])*>", "")
                .replaceAll("(x|X)", "").replaceAll("(d|D)", "");
        int caps = 0;
        char[] split = msg.toCharArray();
        if (split.length < 5) return 0;

        for (char s : split) {
            if (!String.valueOf(s).equals("") && Character.isUpperCase(s)) {
                caps++;
            }
        }

        try {
            return ((caps / split.length) * 100);
        } catch (Exception e) {
            return 0;
        }

    }

    public static int emoteCount(String msg, JDA api) {
        int count = 0;
        Matcher m = EMOJI.matcher(msg);
        while (m.find()) {
            count++;
        }

        List<String> list = new ArrayList<>(Arrays.asList(msg.split(" ")));
        count += checkEmote(list, api);

        return count;
    }

    private static int checkEmote(List<String> list, JDA api) {
        int count = 0;
        for (String s : list) {
            if (Emoji.resolve(s, api) != null) count++;
        }
        return count;
    }

    public static boolean skrotyCount(String[] msg) {
        ArrayList<String> whiteList = new ArrayList<>();
        whiteList.add("jj");
        whiteList.add("jak");
        whiteList.add("juz");
        whiteList.add("już");
        whiteList.add("ja");
        whiteList.add("jem");
        whiteList.add("jez");
        whiteList.add("jej");
        whiteList.add("joł");
        whiteList.add("je");
        whiteList.add("jo");
        whiteList.add("jol");
        whiteList.add("jes");

        for (String s : msg) {
            String pat = s.replaceAll("[^\\u0020\\u0030-\\u0039\\u0041-\\u005A\\u0061-\\u007A\\u00C0-\\u1D99]", "").replaceAll(EMOJI.toString(), "");
            if (whiteList.contains(s.toLowerCase()) || whiteList.contains(pat)) {
                continue;
            }
            if (pat.replaceAll("[jJ][ ]?[a-z-A-Z]{1,2}", "kurwa").equals("kurwa")) return true;
        }

        return false;
    }

}
