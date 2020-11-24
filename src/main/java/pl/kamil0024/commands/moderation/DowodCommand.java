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

package pl.kamil0024.commands.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.internal.entities.AbstractMessage;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.CaseDao;
import pl.kamil0024.core.database.config.CaseConfig;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.DynamicEmbedPageinator;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.core.util.kary.Dowod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("RegExpDuplicateCharacterInClass")
public class DowodCommand extends Command {

    private final CaseDao caseDao;
    private final EventWaiter eventWaiter;

    public DowodCommand(CaseDao caseDao, EventWaiter eventWaiter) {
        name = "dowod";
        aliases.add("dowody");
        permLevel = PermLevel.CHATMOD;
        category = CommandCategory.MODERATION;

        this.caseDao = caseDao;
        this.eventWaiter = eventWaiter;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgs().get(0); // id kary
        String typ = context.getArgs().get(1); // typ lista|usun

        if (arg == null) throw new UsageException();
        if (equalsIgnoreCase(typ, "list", "lista")) {
            Color c = UserUtil.getColor(context.getMember());
            List<FutureTask<EmbedBuilder>> futurePages = new ArrayList<>();
            List<EmbedBuilder> pages = new ArrayList<>();
            CaseConfig cc = caseDao.get(arg);
            if (cc.getKara().getDowody() != null && !cc.getKara().getDowody().isEmpty()) {
                for (Dowod dowod : cc.getKara().getDowody()) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setImage(dowod.getImage());
                    eb.setColor(c);
                    eb.addField("Osoba zgłaszająca:", UserUtil.getFullName(context.getJDA(), dowod.getUser()), false);

                    if (dowod.getContent() != null && !dowod.getContent().isEmpty()) eb.addField("Treść zgłoszenia: ", dowod.getContent(), false);
                    if (dowod.getImage() != null) eb.setImage(getImageUrl(dowod.getImage()));
                    eb.addField(" ", "ID Zgłoszenia: " + dowod.getId(), false);
                    pages.add(eb);
                }
                pages.forEach(p -> futurePages.add(new FutureTask<>(() -> p)));
            } else {
                context.send("Nie ma żadnych dowodów w ten sprawie!").queue();
                return false;
            }
            new DynamicEmbedPageinator(futurePages, context.getUser(), eventWaiter, context.getJDA(), 2137).create(context.getChannel());
            return true;
        }

        if (equalsIgnoreCase(typ, "remove", "delete", "usun", "usuń")) {
            try {
                CaseConfig kara = caseDao.get(arg);
                if (kara.getKara() == null) {
                    context.send("Nie ma kary o takim ID!").queue();
                    return false;
                }
                if (kara.getKara().getDowody() == null) {
                    kara.getKara().setDowody(new ArrayList<>());
                }

                Dowod d = Dowod.getDowodById(Integer.parseInt(context.getArgs().get(2)), kara.getKara().getDowody());
                if (d == null) {
                    context.send("Nie ma dowodu o takim ID!").queue();
                    return false;
                }
                kara.getKara().getDowody().remove(d);
                caseDao.save(kara);
                context.send("Pomyślnie usunięto dowód z tej kary!").queue();
                return true;
            } catch (Exception e) {
                context.send(String.format("Użycie: %sdowod remove <id kary> <id dowodu>", context.getPrefix())).queue();
                return false;
            }
        }

        CaseConfig cc = caseDao.get(arg);
        if (cc.getKara() == null) {
            context.send("Złe ID kary!").queue();
            return false;
        }
        if (cc.getKara().getDowody() == null) cc.getKara().setDowody(new ArrayList<>());

        Dowod d = getKaraConfig(context.getArgsToString(1), context.getMessage());
        d.setId(Dowod.getNextId(cc.getKara().getDowody()));

        cc.getKara().getDowody().add(d);
        caseDao.save(cc);
        context.send("Pomyślnie zapisano dowód!").queue();
        return true;
    }

    private boolean equalsIgnoreCase(String typ, String... s) {
        if (typ == null || typ.isEmpty()) return false;
        for (String s1 : s) {
            if (typ.equalsIgnoreCase(s1)) {
                return true;
            }
        }
        return false;
    }

    public static String getImageUrl(String content) {
        return getImageUrl(new AbstractMessage(content, "fake", false) {
            @Override
            protected void unsupported() {
                throw new IllegalStateException("gay");
            }

            @Nullable
            @Override
            public MessageActivity getActivity() {
                return null;
            }

            @Override
            public long getIdLong() {
                return 0;
            }

            @Nonnull
            @Override
            public List<Attachment> getAttachments() {
                return Collections.emptyList();
            }
        });
    }


    @SuppressWarnings("RegExpRedundantEscape")
    public static String getImageUrl(Message msg) {
        Matcher matcher = Pattern.compile("(http(s)?):\\/\\/(www\\.)?[?a-zA-Z0-9@:-]{2,256}\\.[a-z]{2,24}" +
                        "\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*\\.(a?png|jpe?g|gif|webp|tiff|svg))",
                Pattern.CASE_INSENSITIVE).matcher(msg.getContentRaw());
        if (matcher.find()) return matcher.group();
        if (!msg.getAttachments().isEmpty() && msg.getAttachments().get(0).isImage())
            return msg.getAttachments().get(0).getUrl();
        return null;
    }

    public static Dowod getKaraConfig(String rawTekst, Message msg) {
        Dowod d = new Dowod();
        String content = "";

        if (rawTekst != null && !rawTekst.trim().isEmpty()) content += rawTekst;
        List<Message.Attachment> at = msg.getAttachments();
        if (!at.isEmpty()) d.setImage(at.stream().findAny().get().getUrl());

        if (content.isEmpty() && d.getImage() == null) return null;

        d.setContent(content);
        d.setUser(msg.getAuthor().getId());
        d.setId(1);
        return d;
    }

}
