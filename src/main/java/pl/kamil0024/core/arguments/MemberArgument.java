package pl.kamil0024.core.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
            Member member;
            try {
                member = context.getGuild().retrieveMemberById(prawieUser).complete();
            } catch (ErrorResponseException e) {
                if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER || e.getErrorResponse() == ErrorResponse.UNKNOWN_USER)
                    member = null;
                else throw e;
            }
            if (member != null) return member;
            if (!context.getEvent().getMessage().getMentionedMembers().isEmpty() && context.getEvent().getMessage().getMentionedMembers().get(0) != null)
                return context.getEvent().getMessage().getMentionedMembers().get(0);
        } catch (Exception ignored) {
            if (!context.getEvent().getMessage().getMentionedMembers().isEmpty() && context.getEvent().getMessage().getMentionedMembers().get(0) != null)
                return context.getEvent().getMessage().getMentionedMembers().get(0);
        }

        return null;
    }

}
