package pl.kamil0024.commands.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.database.GiveawayDao;
import pl.kamil0024.core.database.config.GiveawayConfig;
import pl.kamil0024.core.logger.Log;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Data
public class GiveawayListener {

    private static final String TADA = "\uD83C\uDF89";

    @Inject private GiveawayDao giveawayDao;
    @Inject private ShardManager api;

    public List<String> konkursMsg;

    public GiveawayListener(GiveawayDao giveawayDao, ShardManager api) {
        this.giveawayDao = giveawayDao;
        this.api = api;
        List<GiveawayConfig> tak = giveawayDao.getAll();
        this.konkursMsg = new ArrayList<>();
        tak.removeIf(e -> !e.isAktywna() && e.getMessageId() == null);
        tak.forEach(e -> getKonkursMsg().add(String.valueOf(e.getMessageId())));

        ScheduledExecutorService executorSche = Executors.newSingleThreadScheduledExecutor();
        executorSche.scheduleAtFixedRate(this::update, 0, 30, TimeUnit.SECONDS);
    }

    public void createMessage(GiveawayConfig kc) {
        if (kc.getMessageId() != null) throw new UnsupportedOperationException("Próbowano zdublować wiadomość o konkursie " + kc.getId());

        TextChannel tc = api.getTextChannelById(kc.getKanalId());
        if (tc == null) throw new UnsupportedOperationException("Próbowano stworzyć wiadomość o konkursie " + kc.getId() + " ale kanał jest nieprawidłowy");
        if (!tc.canTalk()) throw new UnsupportedOperationException("Próbowano stworzyć konkurs o id " + kc.getId() + " ale bot nie ma permów do pisania na kanale " + tc.getIdLong());

        Message msg = tc.sendMessage(createEmbed(kc).build()).complete();
        msg.addReaction(TADA).queue();
        kc.setMessageId(msg.getId());
        getKonkursMsg().add(msg.getId());
        giveawayDao.save(kc);
    }

    public EmbedBuilder createEmbed(GiveawayConfig kc) {
        EmbedBuilder eb = new EmbedBuilder();

        if (kc.isAktywna()) eb.setColor(Color.cyan);
        else eb.setColor(Color.red);
        eb.setTitle(TADA + " Konkurs");
        eb.addField("Nagroda", kc.getNagroda(), false);
        eb.addField("Wygra osób", String.valueOf(kc.getWygranychOsob()), false);

        User u = api.retrieveUserById(kc.getOrganizator()).complete();
        eb.addField("Organizowany przez", u.getAsMention(), false);
        eb.setThumbnail(u.getAvatarUrl());

        if (kc.isAktywna()) {
            BDate bd = new BDate(ModLog.getLang());
            SimpleDateFormat sfd = new SimpleDateFormat("dd.MM.yyyy @ HH:mm:ss");
            try {
                eb.addField("Koniec o", String.format("%s (%s)",
                        sfd.format(kc.getEnd()),
                        bd.difference(kc.getEnd())), false);
            } catch (Exception e) {
                eb.addField("Koniec o", "? (?)", false);
            }
        } else {
            eb.addField("Koniec o", "Konkurs się zakończył!", false);

            StringBuilder sb = new StringBuilder();
            String f = "<@%s>";
            for (String winner : kc.getWinners()) {
                sb.append(String.format(f, winner)).append("`,` ");
            }
            eb.addField("Wygrali", sb.toString(), false);
        }

        eb.setTimestamp(Instant.now());
        eb.setFooter("ID: " + kc.getId() + " | Ostatnia aktualizacja");

        return eb;
    }

    public void update() {
        xd();
    }

    private synchronized void xd() {
        List<GiveawayConfig> kc = giveawayDao.getAll();
        kc.removeIf(k -> !k.isAktywna());

        for (GiveawayConfig config : kc) {
            TextChannel tc = api.getTextChannelById(config.getKanalId());

            boolean end = false;
            if (config.getEnd() - new Date().getTime() <= 0) {
                config.setAktywna(false);
                end = true;
                new Thread(() -> giveawayDao.save(config)).start();
            }

            Message msg = null;
            try {
                msg = Objects.requireNonNull(tc).retrieveMessageById(config.getMessageId()).complete();
            } catch (Exception ignored) { }

            if (msg == null) {
                config.setMessageId(null);
                createMessage(config);
            }

            if (msg != null && end) {
                ArrayList<String> mozeWygraja = new ArrayList<>();
                ArrayList<String> wygrani = new ArrayList<>();

                for (MessageReaction rec : msg.getReactions()) {
                    if (rec.getReactionEmote().isEmoji() && rec.getReactionEmote().getEmoji().equals(TADA)) {
                        for (User user : rec.retrieveUsers().complete()) {
                            if (!user.isBot()) mozeWygraja.add(user.getId());
                        }
                    }
                }

                Log.debug("Może wygrają: " + new Gson().toJson(mozeWygraja));
                int i = 0;
                Log.debug("losuje " + config.getWygranychOsob() + " wygranych");

                while (i < config.getWygranychOsob()) {
                    Log.debug("Losowanko...");
                    try {
                        if (!mozeWygraja.isEmpty()) {
                            Random rand = new Random();
                            String wygral = mozeWygraja.get(rand.nextInt(mozeWygraja.size() - 1 == 0 ? 1 : mozeWygraja.size() - 1));
                            Log.debug("Jeden z wygranych: " + wygral);
                            wygrani.add(wygral);
                            mozeWygraja.remove(wygral);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    i++;
                    Log.debug("Koniec losowanka");
                }

                Log.debug("Wygrali: " + new Gson().toJson(wygrani));
                config.setWinners(wygrani);
                giveawayDao.save(config);
                msg.clearReactions().queue();

                StringBuilder sb = new StringBuilder();
                String f = "<@%s>";
                String link = "https://discordapp.com/channels/%s/%s/%s";
                link = String.format(link, Ustawienia.instance.bot.guildId, config.getKanalId(), config.getMessageId());
                for (String winner : config.getWinners()) {
                    sb.append(String.format(f, winner)).append("`,` ");
                }
                msg.getChannel().sendMessage(TADA + " Gratulacje dla tych osób:" + sb.toString() +
                        "\nWygrali: " + config.getNagroda() +
                        "\n\n" + link).queue();
            }
            if (msg != null) msg.editMessage(createEmbed(config).build()).queue();

        }
    }

}
