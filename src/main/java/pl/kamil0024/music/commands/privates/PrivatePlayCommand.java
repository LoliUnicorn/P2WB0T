package pl.kamil0024.music.commands.privates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import pl.kamil0024.commands.system.HelpCommand;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.UserUtil;
import pl.kamil0024.music.commands.PlayCommand;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class PrivatePlayCommand extends Command {

    private MusicAPI musicAPI;

    public PrivatePlayCommand(MusicAPI musicAPI) {
        name = "pplay";
        aliases.add("privateplay");
        category = CommandCategory.PRIVATE_CHANNEL;
        this.musicAPI = musicAPI;
    }

    @Override
    public boolean execute(CommandContext context) {
        if (!check(context)) return false;

        String link = context.getArgs().get(0);
        if (link == null) {
            context.send(HelpCommand.getUsage(context).build()).queue();
            return false;
        }

        int wolnyBot = 0;
        MusicRestAction restAction = null;

        for (Member member : PlayCommand.getVc(context.getMember()).getMembers()) {
            if (member.getUser().isBot()) {
                Integer agent = musicAPI.getPortByClient(member.getId());
                if (agent != null) {
                    wolnyBot = agent;
                    restAction = musicAPI.getAction(agent);
                }

            }
        }

        if (wolnyBot == 0 && restAction == null) {
            for (Integer port : musicAPI.getPorts()) {
                restAction = musicAPI.getAction(port);
                if (restAction.getVoiceChannel() == null) {
                    wolnyBot = port;
                    try {
                        MusicResponse tak = restAction.connect(PlayCommand.getVc(context.getMember()));
                    } catch (Exception e) {
                        context.sendTranslate("pplay.dont.connect").queue();
                        return false;
                    }
                    break;
                }
            }
        }

        if (wolnyBot == 0) {
            context.sendTranslate("pplay.to.small.bot").queue();
            return false;
        }

        try {
            MusicResponse play = restAction.play(link.split("v=")[1]);
            if (play.isError() && !play.getError().getDescription().contains("Bot nie jest na żadnym kanale!")) {
                context.send("Nie udało się odtworzyć piosenki! " + play.getError().getDescription()).queue();
                if (restAction.getQueue().isError() && restAction.getPlayingTrack().isError()) {
                    restAction.disconnect();
                }
                return false;
            } else {
                context.sendTranslate("pplay.success").queue();
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            context.sendTranslate("pplay.bad.link").queue();
            try {
                if (restAction.getQueue().isError() && restAction.getPlayingTrack().isError()) {
                    restAction.disconnect();
                }
            } catch (Exception ignored) {}
            return false;
        } catch (Exception e) {
            context.send("Wystąpił błąd z API! " + e.getLocalizedMessage()).queue();
            Log.newError(e);
            try {
                if (restAction.getQueue().isError() && restAction.getPlayingTrack().isError()) {
                    restAction.disconnect();
                }
            } catch (Exception ignored) {}
            return false;
        }
    }

    public static boolean check(CommandContext context) {
        if (!PlayCommand.isVoice(context.getMember())) {
            context.sendTranslate("pplay.no.channel").queue();
            return false;
        }
        VoiceChannel vc = PlayCommand.getVc(context.getMember());
        if (vc.getParent() == null || !vc.getParent().getName().toLowerCase().contains("prywatne kanały")) {
            context.sendTranslate("pplay.no.private").queue();
            return false;
        }


        List<Member> members = vc.getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toList());
        int size = members.size();

        if (!context.getMember().hasPermission(vc, Permission.MANAGE_CHANNEL)) {
            context.sendTranslate("pplay.no.channel.owner").queue();
            return false;
        }

        if (UserUtil.getPermLevel(context.getMember()).getNumer() == PermLevel.MEMBER.getNumer()) {
            if (leave(vc)) {
                context.sendTranslate("pplay.min.members").queue();
                return false;
            }
        }

        return true;
    }

    public static boolean leave(VoiceChannel vc) {
        List<Member> members = vc.getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toList());

        boolean jestAdm = false;
        for (Member member : members) {
            try {
                String nick = member.getNickname();
                if (nick == null) continue;

                if (nick.startsWith("[POM]") || nick.startsWith("[MOD]") || nick.startsWith("[ADM]")) {
                    jestAdm = true;
                    break;
                }

            } catch (Exception ignored) {}
        }

        if (jestAdm) return false;

        return members.size() < 4;
    }

}
