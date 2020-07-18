package pl.kamil0024.commands.dews;

import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ShellCommand extends Command {

    public ShellCommand() {
        name = "shell";
        permLevel = PermLevel.DEVELOPER;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();

        String result = shell(arg);
        if (result == null) {
            context.send("Result jest nullem??").queue();
            return false;
        }

        if (result.length() > 1993) result = result.substring(0, 1993);
        context.send("```" + result + "```").queue();
        return true;
    }

    public static String shell(String code) {
        try {
            Process process = new ProcessBuilder
                    (Arrays.asList(System.getenv("SHELL"), "-c", code))
                    .redirectErrorStream(true).start();
            process.waitFor(5, TimeUnit.MINUTES);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            if (reader.ready()) {
                String line;
                while ((line = reader.readLine()) != null) builder.append(line).append(System.lineSeparator());
                reader.close();
            }
            process.destroyForcibly();
            String result = builder.toString();
            return result;
        } catch (Exception ignored) { }
        return null;
    }

}
