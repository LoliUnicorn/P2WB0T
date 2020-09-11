/*
 * Copyright (C) 2019-2020 FratikB0T Contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package pl.kamil0024.commands.zabawa;

import com.github.francesco149.koohii.Koohii;
import com.google.common.eventbus.EventBus;
import com.oopsjpeg.osu4j.GameMod;
import com.oopsjpeg.osu4j.OsuBeatmap;
import com.oopsjpeg.osu4j.OsuScore;
import com.oopsjpeg.osu4j.OsuUser;
import com.oopsjpeg.osu4j.backend.EndpointUserBests;
import com.oopsjpeg.osu4j.backend.EndpointUserRecents;
import com.oopsjpeg.osu4j.backend.EndpointUsers;
import com.oopsjpeg.osu4j.backend.Osu;
import com.oopsjpeg.osu4j.exception.OsuAPIException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.util.EmbedPageintaor;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.NetworkUtil;
import pl.kamil0024.core.util.UsageException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.FutureTask;

public class OsuCommand extends Command {

    private final Osu osu;
    private final ShardManager shardManager;
    private final EventWaiter eventWaiter;

    public OsuCommand(ShardManager shardManager, EventWaiter eventWaiter) {
        name = "osu";
        category = CommandCategory.ZABAWA;
        cooldown = 5;
        this.shardManager = shardManager;
        this.eventWaiter = eventWaiter;
        this.osu = Osu.getAPI(Ustawienia.instance.osu.apiKey);
    }

    @Override
    protected boolean execute(@NotNull CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null || arg.isEmpty() || context.getArgs().get(1) == null) throw new UsageException();
        if (arg.equalsIgnoreCase("user")) {
            NumberFormat nf = NumberFormat.getInstance();
            Message mes = context.send(context.getTranslate("generic.loading")).complete();
            try {
                OsuUser u = osu.users.query(new EndpointUsers.ArgumentsBuilder(context.getArgs().get(1)).build());
                if (u == null) {
                    mes.editMessage(context.getTranslate("osu.user.not.found")).complete();
                    return false;
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(u.getUsername(), u.getURL().toString(), "https://a.ppy.sh/" + u.getID());
                eb.addField(context.getTranslate("osu.user.accuracy"),
                        round(u.getAccuracy(), 2, RoundingMode.HALF_UP) + "%", true);
                eb.addField(context.getTranslate("osu.user.ranks"), String.format("%s %s\n%s %s\n%s %s\n%s %s\n%s %s",
                        getEmotka(Ustawienia.instance.osu.osuSSH), u.getCountRankSSH(), getEmotka(Ustawienia.instance.osu.osuSS),
                        u.getCountRankSS(), getEmotka(Ustawienia.instance.osu.osuSH), u.getCountRankSH(),
                        getEmotka(Ustawienia.instance.osu.osuS), u.getCountRankS(), getEmotka(Ustawienia.instance.osu.osuA),
                        u.getCountRankA()), true);
                eb.addField(context.getTranslate("osu.user.country"),
                        ":flag_" + u.getCountry().name().toLowerCase() + ":", true);
                eb.addField(context.getTranslate("osu.user.rank"), context.getTranslate("osu.user.rank.text",
                        u.getRank(), u.getCountryRank()), true);
                eb.addField(context.getTranslate("osu.user.total.hits"), nf.format(u.getTotalHits()), true);
                eb.addField(context.getTranslate("osu.user.total.score"), nf.format(u.getTotalScore()), true);
                eb.addField(context.getTranslate("osu.user.total.ranked.score"), nf.format(u.getRankedScore()), true);
                eb.addField(context.getTranslate("osu.user.pp"), nf.format(u.getPPRaw()), true);
                eb.addField(context.getTranslate("osu.user.level"), nf.format(round(u.getLevel(), 2,
                        RoundingMode.HALF_UP)), true);
                eb.addField(context.getTranslate("osu.user.play.count"), nf.format(u.getPlayCount()), true);
                eb.addField(context.getTranslate("osu.user.played"),
                        humanReadableFormat(u.getTotalSecondsPlayed() * 1000, false),
                        true);
                eb.setColor(context.getMember().getColorRaw());
                mes.editMessage(eb.build()).override(true).complete();
            } catch (OsuAPIException | MalformedURLException e) {
                mes.editMessage(context.getTranslate("osu.error")).queue();
                return false;
            }
            return true;
        }
        if (arg.equalsIgnoreCase("topplay")) {
            Message mes = context.send(context.getTranslate("generic.loading")).complete();
            try {
                java.util.List<OsuScore> wyniki = osu.userBests
                        .query(new EndpointUserBests.ArgumentsBuilder(context.getArgs().get(1)).setLimit(100).build());
                if (wyniki.isEmpty()) {
                    mes.editMessage(context.getTranslate("osu.topplay.empty")).queue();
                    return false;
                }
                renderScores(context, mes, wyniki);
            } catch (OsuAPIException e) {
                mes.editMessage(context.getTranslate("osu.error")).queue();
                return false;
            }
            return true;
        }
        if (arg.equalsIgnoreCase("recentplay")) {
            Message mes = context.send(context.getTranslate("generic.loading")).complete();
            try {
                java.util.List<OsuScore> wyniki = osu.userRecents
                        .query(new EndpointUserRecents.ArgumentsBuilder(context.getArgs().get(1)).setLimit(50).build());
                if (wyniki.isEmpty()) {
                    mes.editMessage(context.getTranslate("osu.recentplay.empty")).queue();
                    return false;
                }
                renderScores(context, mes, wyniki);
            } catch (OsuAPIException e) {
                mes.editMessage(context.getTranslate("osu.error")).queue();
                return false;
            }
            return true;
        }
        return false;
    }

    private void renderScores(@NotNull CommandContext context, Message mes, java.util.List<OsuScore> wyniki) {
        java.util.List<EmbedBuilder> pages = new ArrayList<>();
        for (OsuScore w : wyniki) {
            try {
                pages.add(renderScore(context, w));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new EmbedPageintaor(pages, mes.getAuthor(), eventWaiter, mes.getJDA(), 120).create(mes);
    }

    @NotNull
    private EmbedBuilder renderScore(@NotNull CommandContext context, OsuScore w) throws IOException {
        NumberFormat nf = NumberFormat.getInstance();
        OsuUser u = w.getUser().get();
        OsuBeatmap m = w.getBeatmap().get();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter("%s/%s");
        eb.setAuthor(u.getUsername(), u.getURL().toString(), "https://a.ppy.sh/" + u.getID());
        eb.addField(context.getTranslate("osu.score.beatmap"), generateBeatmapString(m),
                false);
        eb.addField(context.getTranslate("osu.score.score"),
                generateScore(w), true);
        eb.addField("", generateScoreSecLine(w), true);
        eb.addField("", generateScoreThirdLine(w), true);
        // Inspirowane https://github.com/AznStevy/owo/blob/develop/cogs/osu.py
        Koohii.Map map = null;
        if (isPass(w)) {
            Double pp = null;
            if (w.getPp() == 0) {
                map = new Koohii.Parser()
                        .map(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(NetworkUtil
                                .download("https://osu.ppy.sh/osu/" + w.getBeatmapID())))));
                Koohii.PPv2Parameters p = new Koohii.PPv2Parameters();
                p.mods = Math.toIntExact(GameMod.getBit(w.getEnabledMods()));
                Koohii.DiffCalc dc = new Koohii.DiffCalc().calc(map, p.mods);
                p.aim_stars = dc.aim;
                p.speed_stars = dc.speed;
                p.max_combo = m.getMaxCombo();
                p.nsliders = map.nsliders;
                p.ncircles = map.ncircles;
                p.nobjects = map.objects.size();
                p.base_ar = map.ar;
                p.base_od = map.od;
                p.mode = map.mode;
                p.combo = w.getMaxCombo();
                p.n300 = w.getHit300();
                p.n100 = w.getHit100();
                p.n50 = w.getHit50();
                p.nmiss = w.getMisses();
                p.score_version = 1;
                p.beatmap = map;
                pp = new Koohii.PPv2(p).total;
            }
            eb.addField(context.getTranslate("osu.score.pp"),
                    nf.format(round(w.getPp() != 0 ? w.getPp() : pp, 2, RoundingMode.HALF_UP)), true);
            if (pp != null) {
                eb.setFooter(context.getTranslate("osu.score.pp.self.calc"));
            }
        }
        if (!isPass(w)) {
            if (map == null) map = new Koohii.Parser()
                    .map(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(NetworkUtil
                            .download("https://osu.ppy.sh/osu/" + w.getBeatmapID())))));
            java.util.List<Double> dList = new ArrayList<>();
            for (Koohii.HitObject obj : map.objects) {
                dList.add(obj.time);
            }
            Double objPierwszy = dList.get(0);
            Double objOstatni = dList.get(dList.size() - 1);
            Double objOstatniKlikniety = dList.get(w.getHit50() + w.getHit100() + w.getHit300() + w.getMisses() - 1);
            Double timing = objOstatni - objPierwszy;
            Double point = objOstatniKlikniety - objPierwszy;
            eb.addField(context.getTranslate("osu.score.completion"), nf.format(round((point / timing)
                    * 100, 2, RoundingMode.HALF_UP)) + "%", true);
        }
        eb.addField(context.getTranslate("osu.score.combo"), w.getMaxCombo() + "x", true);
        if (isPass(w)) {
            eb.addField(context.getTranslate("osu.score.fc"), w.isPerfect() ?
                    context.getTranslate("generic.yes") : context.getTranslate("generic.no"), true);
        }
        eb.addField(context.getTranslate("osu.score.mods"), getMods(w), true);
        eb.addField(context.getTranslate("osu.score.acc"),
                round(calcAcc(w) * 100, 2, RoundingMode.HALF_UP) + "%", true);
        eb.addField(context.getTranslate("osu.score.replay"), w.isReplayAvailable() ?
                        context.getTranslate("osu.score.replay.download", "https://osu.ppy.sh/scores/osu/" +
                                w.getScoreID() + "/download") : context.getTranslate("osu.score.replay.unavailable"),
                true);
        eb.setTimestamp(w.getDate());
        String imgUrl = "https://assets.ppy.sh/beatmaps/" + m.getBeatmapSetID() + "/covers/cover.jpg";
        eb.setImage(imgUrl);
        eb.setColor(getColor(w.getRank()));
        return eb;
    }

    private boolean isPass(OsuScore w) {
        return !w.getRank().equals("F");
    }

    private double calcAcc(OsuScore w) {
        return (double) (50 * w.getHit50() + 100 * w.getHit100() + 300 * w.getHit300()) /
                (300 * (w.getMisses() + w.getHit50() + w.getHit100() + w.getHit300()));
    }

    private String getMods(OsuScore w) {
        if (w.getEnabledMods().length == 0) return "No Mods";
        StringBuilder sb = new StringBuilder();
        for (GameMod m : w.getEnabledMods()) {
            try {
                sb.append(getEmotka((String) Ustawienia.instance.osu.getClass().getDeclaredField("osu" +
                        getOsuShortMod(m)).get(Ustawienia.instance.osu)).getAsMention()).append("\n");
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException | OsuAPIException e) {
                sb.append(m.getName()).append("\n");
            }
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String getOsuShortMod(GameMod m) throws OsuAPIException {
        switch (m) {
            case EASY:
                return "EZ";
            case KEY_1:
                return "1K";
            case KEY_2:
                return "2K";
            case KEY_3:
                return "3K";
            case KEY_4:
                return "4K";
            case KEY_5:
                return "5K";
            case KEY_6:
                return "6K";
            case KEY_7:
                return "7K";
            case KEY_8:
                return "8K";
            case KEY_9:
                return "9K";
            case RELAX:
                return "RX";
            case CINEMA:
                return "CN";
            case HIDDEN:
                return "HD";
            case RANDOM:
                return "RD";
            case TARGET:
                return "TP";
            case FADE_IN:
                return "FI";
            case NO_FAIL:
                return "NF";
            case PERFECT:
                return "PF";
            case SPUNOUT:
                return "SO";
            case AUTOPLAY:
                return "AO";
            case KEY_COOP:
                return "COOPK";
            case LAST_MOD:
                return "LM";
            case SCORE_V2:
                return "V2";
            case AUTOPILOT:
                return "AP";
            case HALF_TIME:
                return "HT";
            case HARD_ROCK:
                return "HR";
            case NIGHTCORE:
                return "NC";
            case FLASHLIGHT:
                return "FL";
            case DOUBLE_TIME:
                return "DT";
            case SUDDEN_DEATH:
                return "SD";
            case TOUCH_DEVICE:
                return "TD";
        }
        throw new OsuAPIException("kurwa co to za mod");
    }

    private String generateScore(OsuScore w) {
        return String.format("**%s**\n%s", w.getScore(), getRank(w));
    }

    private String getRank(OsuScore w) {
        String rank = w.getRank();
        switch (rank) {
            case "XH":
                return checkEmotkiStr(Ustawienia.instance.osu.osuSSH) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuSSH)).getAsMention() : "XH";
            case "X":
                return checkEmotkiStr(Ustawienia.instance.osu.osuSS) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuSS)).getAsMention() : "X";
            case "SH":
                return checkEmotkiStr(Ustawienia.instance.osu.osuSH) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuSH)).getAsMention() : "SH";
            case "S":
                return checkEmotkiStr(Ustawienia.instance.osu.osuS) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuS)).getAsMention() : "S";
            case "A":
                return checkEmotkiStr(Ustawienia.instance.osu.osuA) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuA)).getAsMention() : "A";
            case "B":
                return checkEmotkiStr(Ustawienia.instance.osu.osuB) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuB)).getAsMention() : "B";
            case "C":
                return checkEmotkiStr(Ustawienia.instance.osu.osuC) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuC)).getAsMention() : "C";
            case "D":
                return checkEmotkiStr(Ustawienia.instance.osu.osuD) ? Objects.requireNonNull(shardManager
                        .getEmoteById(Ustawienia.instance.osu.osuD)).getAsMention() : "D";
            case "F":
                return "FAIL";
            default:
                return "";
        }
    }

    private String generateScoreSecLine(OsuScore w) {
        return String.format("%s %s\n%s %s\n%s %s",
                getEmotka(Ustawienia.instance.osu.osu300), w.getHit300(),
                getEmotka(Ustawienia.instance.osu.osu100), w.getHit100(),
                getEmotka(Ustawienia.instance.osu.osu50), w.getHit50());
    }

    private String generateScoreThirdLine(OsuScore w) {
        return String.format("%s %s\n%s %s\n%s %s", getEmotka(Ustawienia.instance.osu.osugeki), w.getGekis(),
                getEmotka(Ustawienia.instance.osu.osukatu), w.getKatus(),
                getEmotka(Ustawienia.instance.osu.osumiss), w.getMisses());
    }

    private boolean checkEmotki() {
        return true;
    }

    private boolean checkEmotkiStr(String uwuOwo) {
        return uwuOwo != null && !uwuOwo.isEmpty() && shardManager.getEmoteById(uwuOwo) != null;
    }

    private Emote getEmotka(String id) {
        if (id == null || id.isEmpty()) return null;
        return shardManager.getEmoteById(id);
    }

    private String generateBeatmapString(OsuBeatmap m) throws MalformedURLException {
        return m.getTitle() +"\n" + m.getArtist() + " // " + m.getCreatorName() + "\n" + "**" + m.getVersion() + "**" +
                " [" + round(m.getDifficulty(), 2, RoundingMode.HALF_UP) + " \u2605]" +
                "\n[Link](" + m.getURL().toString() + ")" + " [osu!direct](https://fratikbot.pl/osu/b/" + m.getID() + ")";
    }

    private Color getColor(String rank) {
        switch (rank) {
            case "XH":
                return new Color(0xBDBDBD);
            case "X":
                return new Color(0xFFBC0D);
            case "SH":
                return new Color(0xE2E2E2);
            case "S":
                return new Color(0xFF7F31);
            case "A":
                return new Color(0x5CCA0B);
            case "B":
                return new Color(0x0562E7);
            case "C":
                return new Color(0xA917D7);
            case "D":
                return new Color(0xCA0010);
            case "F":
                return new Color(0XFF0000);
        }
        return null;
    }

    private double round(double value, int scale, RoundingMode mode) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, mode);
        return bd.doubleValue();
    }

    private String humanReadableFormat(long millis, boolean excludeSeconds) {
        org.joda.time.Duration durejszon = new org.joda.time.Duration(millis);
        PeriodFormatter formatter;
        if (excludeSeconds) {
            formatter = new PeriodFormatterBuilder()
                    .appendYears()
                    .appendSuffix("y ")
                    .appendWeeks()
                    .appendSuffix("w ")
                    .appendDays()
                    .appendSuffix("d ")
                    .appendHours()
                    .appendSuffix("h ")
                    .appendMinutes()
                    .appendSuffix("m ")
                    .toFormatter();
        } else {
            formatter = new PeriodFormatterBuilder()
                    .appendYears()
                    .appendSuffix("y ")
                    .appendWeeks()
                    .appendSuffix("w ")
                    .appendDays()
                    .appendSuffix("d ")
                    .appendHours()
                    .appendSuffix("h ")
                    .appendMinutes()
                    .appendSuffix("m ")
                    .appendSeconds()
                    .appendSuffix("s ")
                    .toFormatter();
        }
        return formatter.print(durejszon.toPeriod().normalizedStandard()).replaceAll(" 0[wdhms]", "");
    }

}
