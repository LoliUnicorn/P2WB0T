package pl.kamil0024.core.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import pl.kamil0024.core.Ustawienia;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("unused")
@Data
public class WebhookUtil {

    LogType type;
    String name;
    String message;
    String avatar;
    WebhookEmbed embed;
    String time = new SimpleDateFormat("MM.dd HH:mm:ss").format(Calendar.getInstance().getTime());

    public WebhookUtil() {}

    public void send() {
        if (type == null) throw new NullPointerException("type == null");
        WebhookClient client = WebhookClient.withUrl(getType().url);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        builder.setUsername(getType().slownie);

        if (!getMessage().isEmpty()) builder.setContent(String.format("[%s] %s", getTime(), getMessage()));
        if (getEmbed() != null) builder.addEmbeds(getEmbed());

        builder.setAvatarUrl(getAvatar());
        client.send(builder.build());
        client.close();
    }

    @SuppressWarnings("ConstantConditions")
    public static WebhookEmbed getWebhookEmbed(EmbedBuilder eb) {
        MessageEmbed msg = eb.build();
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder();
        try {
            if (msg.getAuthor().getName() != null) builder.setAuthor(
                    new WebhookEmbed.EmbedAuthor(msg.getAuthor().getName(), msg.getAuthor().getIconUrl(), msg.getAuthor().getUrl())
            );
        } catch (Exception ignored) {}
        try {
            if (msg.getFooter().getText() != null) {
                builder.setFooter(new WebhookEmbed.EmbedFooter(msg.getFooter().getText(), msg.getFooter().getIconUrl()));
            }
        } catch (Exception ignored) {}
        try {
            builder.setImageUrl(msg.getImage().getUrl());
        } catch (Exception ignored) {}

        try {
            msg.getFields().forEach(f -> builder.addField(getField(f)));
        } catch (Exception ignored) {}

        try {
            builder.setTitle(new WebhookEmbed.EmbedTitle(msg.getTitle(), msg.getUrl()));
        } catch (Exception ignored) {}

        try {
            builder.setDescription(msg.getDescription());
        } catch (Exception ignored) {}

        try {
            builder.setColor(msg.getColor().getRGB());
        } catch (Exception ignored) {}

        try {
            builder.setTimestamp(msg.getTimestamp());
        } catch (Exception ignored) {}

        try {
            builder.setThumbnailUrl(msg.getThumbnail().getUrl());
        } catch (Exception ignored) {}
        return builder.build();
    }

    @SuppressWarnings("ConstantConditions")
    private static WebhookEmbed.EmbedField getField(MessageEmbed.Field f) {
        return new WebhookEmbed.EmbedField(f.isInline(), f.getName(), f.getValue());
    }

    public enum LogType {

        ERROR("Logi errorów", Ustawienia.instance.webhook.error),
        CMD("Logi komend", Ustawienia.instance.webhook.cmd),
        DEBUG("Logi debugu", Ustawienia.instance.webhook.debug),
        STATUS("Logi statusów", Ustawienia.instance.webhook.status);

        @Getter private final String slownie;
        @Getter private final String url;

        LogType(String slownie, String url) {
            this.slownie = slownie;
            this.url = url;
        }

    }
}
