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
