package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.EmbedBuilder;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.util.UserUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ForumCommand extends Command {

    public ForumCommand() {
        name = "forum";
        cooldown = 60;
    }

    @Override
    public boolean execute(CommandContext context) {
        EmbedBuilder eb = new EmbedBuilder();
        HashMap<String, String> tak = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String oj = "[%s](%s)";
        eb.setColor(UserUtil.getColor(context.getMember()));
        eb.setTitle(context.getTranslate("forum.cate"));
        eb.setFooter("Forum", "https://images-ext-1.discordapp.net/external/nEimyViUHXsLUA2ltyosXai_uLaOdc0eeWhcINuYGrM/https/derpmc.pl/assets/images/DerpMC/header_logo.png");
        eb.setTimestamp(Instant.now());

        tak.put("P2W", "https://p2w.pl/");
        tak.put("Zgłoś błąd na serwerze", "https://p2w.pl/forum/11-zg%C5%82o%C5%9B-b%C5%82%C4%85d-na-serwerze/");
        tak.put("Propozycje", "https://p2w.pl/forum/12-propozycje/");
        tak.put("Pytania i problemy", "https://p2w.pl/forum/25-pytania-i-problemy/");
        tak.put("Zgłoś gracza", "https://p2w.pl/forum/8-zg%C5%82o%C5%9B-gracza/");
        tak.put("Odwołanie od bana", "https://p2w.pl/forum/9-odwo%C5%82anie-od-bana/");
        tak.put("Problem z płatnościami", "https://p2w.pl/forum/26-problem-z-p%C5%82atno%C5%9Bciami/");
        tak.put("Problemy z kontem", "https://p2w.pl/forum/57-problemy-z-kontem/");
        tak.put("Zdjęcie logowania premium", "https://p2w.pl/forum/47-zdj%C4%99cie-logowania-premium/");
        tak.put("Rekrutacja", "https://p2w.pl/forum/38-rekrutacja/");

        for (Map.Entry<String, String> xd : tak.entrySet()) {
            sb.append(String.format(oj, xd.getKey(), xd.getValue())).append("\n—————————————\n");
        }
        eb.setDescription(sb.toString());
        context.send(eb.build()).queue();
        return true;
    }

}
