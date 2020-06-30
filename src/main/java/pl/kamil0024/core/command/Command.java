package pl.kamil0024.core.command;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    @Getter protected String name;
    @Getter protected int cooldown = 0;
    @Getter protected CommandCategory category = CommandCategory.SYSTEM;
    @Getter protected PermLevel permLevel = PermLevel.MEMBER;
    @Getter protected List<String> aliases = new ArrayList<>();
    
    protected boolean execute(@NotNull CommandContext context) throws Exception, UsageException {
        throw new UnsupportedOperationException("Komenda nie ma zaimplementowanej funkcji execute()");
    }

    @Override
    public String toString() {
        return this.name;
    }
}
