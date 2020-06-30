package pl.kamil0024.core.arguments;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import pl.kamil0024.core.command.CommandContext;

public abstract class Args {

    @Getter protected String name;

    public Object parsed(String o, JDA jda, CommandContext context) throws Exception {
        throw new UnsupportedOperationException("Argu nie ma zaimplementowanej funkcji execute()");
    }

    @Override
    public String toString() {
        return this.name;
    }

}
