package pl.kamil0024.core.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.command.CommandContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
public class MemberArgument extends Args {

    private static final Pattern MENTION_REGEX = Pattern.compile("<@!?(\\d{17,18})>");
    private static final Pattern TAG_REGEX = Pattern.compile("(.{2,32}#\\d{4})");

    public MemberArgument() {
        name = "member";
    }

    @Override
    public Member parsed(String s, @Nullable JDA jda, CommandContext context) {
        if (jda == null) throw new UnsupportedOperationException("jda==null");
        String prawieUser = String.valueOf(s);
        if (prawieUser == null) return null;
        try {
            try {
                if (context.getGuild().getMemberById(prawieUser) != null)
                    return context.getGuild().getMemberById(prawieUser);
            } catch (Exception e1) {
                // nic
            }
            Matcher matcher = MENTION_REGEX.matcher(prawieUser);
            if (matcher.matches()) {
                return context.getGuild().getMemberById(matcher.group(1));
            }
            Matcher matcher1 = TAG_REGEX.matcher(prawieUser);
            if (matcher1.matches()) {
                List<Member> ul = context.getGuild().getMembers().stream().filter(u ->
                        u.getUser().getAsTag().equals(matcher1.group(1)))
                        .collect(Collectors.toList());
                if (ul.size() == 1) return ul.get(0);
            }
        } catch (Exception ignored) {
            /* lul */
        }
        return null;
    }

}
