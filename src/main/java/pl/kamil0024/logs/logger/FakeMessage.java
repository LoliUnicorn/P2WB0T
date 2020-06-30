package pl.kamil0024.logs.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

import java.time.OffsetDateTime;

@AllArgsConstructor
public class FakeMessage {

    @Getter public final String id;
    @Getter public final String author;
    @Getter public final String content;
    @Getter public final String channel;
    @Getter public final OffsetDateTime createdAt;

    public static FakeMessage convert(Message msg) {
        return new FakeMessage(msg.getId(),
                msg.getAuthor().getId(),
                msg.getContentRaw(),
                msg.getTextChannel().getId(), msg.getTimeCreated());
    }

}
