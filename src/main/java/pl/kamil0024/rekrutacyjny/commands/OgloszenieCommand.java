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

package pl.kamil0024.rekrutacyjny.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.WebhookUtil;

import java.util.List;

public class OgloszenieCommand extends Command {

    public OgloszenieCommand() {
        name = "ogloszenia";
        aliases.add("ogloszenie");
        aliases.add("ogłoszenie");
        aliases.add("ogłoszenia");
        permLevel = PermLevel.MODERATOR;
        enabledInRekru = true;
        onlyInRekru = true;
    }

    @Override
    public boolean execute(CommandContext context) {
        TextChannel txt = context.getJDA().getTextChannelById(Ustawienia.instance.rekrutacyjny.ogloszeniaId);
        if (txt == null) {
            context.sendTranslate("ogloszenia.nullchannel").queue();
            Log.newError(context.getTranslate("ogloszenia.nullchannel"), getClass());
            return false;
        }
        if (!txt.canTalk() || !context.getGuild().getSelfMember().hasPermission(txt, Permission.MESSAGE_EMBED_LINKS, Permission.MANAGE_WEBHOOKS)) {
            context.sendTranslate("ogloszenia.noperms", txt.getAsMention()).queue();
            return false;
        }
        String arg = context.getArgsToString(0);
        if (arg == null || arg.trim().isEmpty()) {
            context.sendTranslate("ogloszenia.requireargs").queue();
            return false;
        }

        Webhook web = txt.retrieveWebhooks().complete().stream().findAny().orElse(txt.createWebhook(context.getTranslate("ogloszenia.webhookname")).complete());
        if (web == null) throw new NullPointerException("Webhook jest nullem!");
        WebhookUtil wu = new WebhookUtil();
        wu.setUrl(web.getUrl().replaceAll("discord\\.com", "discordapp.com"));
        wu.setMessage(arg + "\n" + context.getGuild().getPublicRole().getAsMention());
        wu.setName(context.getMember().getNickname() == null ? context.getUser().getName() : context.getMember().getNickname());
        wu.setAvatar(context.getUser().getAvatarUrl());

        List<Message.Attachment> at = context.getMessage().getAttachments();
        if (!at.isEmpty()) {
            at.stream().findAny().get().retrieveInputStream()
                    .thenAccept(wu::sendNormalMessage)
                    .exceptionally(t -> {
                        Log.newError(t, getClass());
                        context.sendTranslate("ogloszenia.imageerror").queue();
                        return null;
                    });
        } else wu.sendNormalMessage(null);

        context.send("Pomyślnie wysłano ogłoszenie!").queue();
        return true;
    }

}
