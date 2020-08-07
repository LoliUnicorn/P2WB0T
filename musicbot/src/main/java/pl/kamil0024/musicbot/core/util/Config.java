package pl.kamil0024.musicbot.core.util;

import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    boolean hidden() default false;

    Entities entity() default Entities.STRING;

    enum Entities {
        ROLE, CHANNEL, STRING;

        public static Class<?> getEntity(Entities e) {
            switch (e) {
                case ROLE: return Role.class;
                case CHANNEL: return GuildChannel.class;
                case STRING: return String.class;
            }
            return null;
        }
    }
}
