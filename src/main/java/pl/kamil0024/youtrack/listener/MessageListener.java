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

package pl.kamil0024.youtrack.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.youtrack.YouTrack;
import pl.kamil0024.youtrack.models.Issue;

import javax.annotation.Nonnull;
import java.time.Instant;

public class MessageListener extends ListenerAdapter {

    private YouTrack youTrack;

    public MessageListener(YouTrack youTrack) {
        this.youTrack = youTrack;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent e) {

    }

    private MessageEmbed generateEmbed(Issue i) {
        Issue.Field priorytet = null;
        Issue.Field typ = null;
        Issue.Field status = null;
        Issue.Field przypisane = null;
        Issue.Field trybGry = null;
        Issue.Field wersjaMc = null;
        Issue.Field serwer = null;
        Issue.Field arcade = null;
        Issue.Field tester = null;
        Issue.Field wynikTestu = null;
        Issue.Field nickZglaszajacego = null;
        for (Issue.Field f : i.getFields()) {
            if (f.getName().equals("Priorytet")) priorytet = f;
            if (f.getName().equals("Typ")) typ = f;
            if (f.getName().equals("Status")) status = f;
            if (f.getName().equals("Przypisane Do")) przypisane = f;
            if (f.getName().equals("Tryb Gry")) trybGry = f;
            if (f.getName().equals("Wersja Minecrafta")) wersjaMc = f;
            if (f.getName().equals("Serwer")) serwer = f;
            if (f.getName().equals("Tryby Na Arcade")) arcade = f;
            if (f.getName().equals("Tester")) tester = f;
            if (f.getName().equals("Wyniki Testu")) wynikTestu = f;
            if (f.getName().equals("Nick zgłaszającego")) nickZglaszajacego = f;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(i.getIdReadable());
        eb.setTitle(i.getSummary());
        eb.setDescription(i.getDescription());
        eb.setColor(priorytet.getValue().get(0).getColor().getBackground());
        eb.setFooter(i.getReporter().getFullName(), i.getReporter().getAvatarUrl());
        eb.setTimestamp(Instant.ofEpochMilli(i.getCreated())).build();

        String info = "Priorytet: %s\nTyp: %s\nStatus: %s\nPrzypisane do: %s";
        eb.addField("Podstawowe Informacje",
                String.format(info,
                        priorytet.getValue().get(0).getName(),
                        typ.getValue().get(0).getName(),
                        status.getValue().get(0).getName(),
                        przypisane.getValue().get(0).getName()),
                false);
        String trybGryValue = trybGry.getValue().get(0).getName();
        switch (trybGryValue) {
            case "Budowlany":
            case "Lobby":
            case "BedWars":
            case "SkyWars":
            case "Murder Mystery":
            case "Housing | Freebuild (stara edycja)":
            case "Arcade Games":
            case "Build Battle":
            case "Forum":
        }

        return new EmbedBuilder().setAuthor(i.getIdReadable()).setTitle(i.getSummary())
                .setDescription(i.getDescription()).setColor(priorytet.getValue().get(0).getColor().getBackground())
                .addField("Priorytet", priorytet.getValue().get(0).getName(), true)
                .addField("Typ", typ.getValue().get(0).getName(), true)
                .addField("Stan", status.getValue().get(0).getName(), true)
                .addField("Przypisane do", przypisane.getValue().get(0).getName(), true)
                .addField("Tester", tester.getValue().get(0).getName(), true)
                .addField("Wyniki testu", wynikTestu.getValue().get(0).getName(), true)
                .addField("Otwórz w przeglądarce", "[Klik](" + Ustawienia.instance.yt.url +
                        "/issue/" + i.getIdReadable() + ")", false)
                .setFooter(i.getReporter().getFullName(), i.getReporter().getAvatarUrl())
                .setTimestamp(Instant.ofEpochMilli(i.getCreated())).build();
    }

}
