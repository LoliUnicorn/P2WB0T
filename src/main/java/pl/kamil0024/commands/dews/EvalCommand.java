package pl.kamil0024.commands.dews;

import com.google.inject.Inject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.dv8tion.jda.api.EmbedBuilder;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.commands.listener.GiveawayListener;
import pl.kamil0024.commands.system.HelpCommand;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.CommandExecute;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.command.enums.CommandCategory;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.database.*;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.core.util.Tlumaczenia;
import pl.kamil0024.core.util.kary.KaryJSON;
import pl.kamil0024.musicmanager.MusicManager;
import pl.kamil0024.musicmanager.impl.MusicManagerImpl;

import java.awt.*;

public class EvalCommand extends Command {

    @Inject private final EventWaiter eventWaiter;
    @Inject private final CommandManager commandManager;
    @Inject private final CaseDao caseDao;
    @Inject private final ModLog modLog;
    @Inject private final KaryJSON karyJSON;
    @Inject private final Tlumaczenia tlumaczenia;
    @Inject private final CommandExecute commandExecute;
    @Inject private final UserDao userDao;
    @Inject private final NieobecnosciDao nieobecnosciDao;
    @Inject private final RemindDao remindDao;
    @Inject private final ModulManager modulManager;
    @Inject private final GiveawayListener giveawayListener;
    @Inject private final GiveawayDao giveawayDao;

    public EvalCommand(EventWaiter eventWaiter, CommandManager commandManager, CaseDao caseDao, ModLog modLog, KaryJSON karyJSON, Tlumaczenia tlumaczenia, CommandExecute commandExecute, UserDao userDao, NieobecnosciDao nieobecnosciDao, RemindDao remindDao, ModulManager modulManager, GiveawayListener giveawayListener, GiveawayDao giveawayDao) {
        name = "eval";
        aliases.add("ev");
        category = CommandCategory.DEVS;
        permLevel = PermLevel.DEVELOPER;
        this.eventWaiter = eventWaiter;
        this.commandManager = commandManager;
        this.caseDao = caseDao;
        this.modLog = modLog;
        this.karyJSON = karyJSON;
        this.tlumaczenia = tlumaczenia;
        this.commandExecute = commandExecute;
        this.userDao = userDao;
        this.nieobecnosciDao = nieobecnosciDao;
        this.remindDao = remindDao;
        this.modulManager = modulManager;
        this.giveawayListener = giveawayListener;
        this.giveawayDao = giveawayDao;
    }

    @Override
    public boolean execute(CommandContext context) {
        String kod = context.getMessage().getContentRaw();
        if (kod.isEmpty()) {
            context.getChannel().sendMessage(HelpCommand.getUsage(context).build()).queue();
            return false;
        }
        kod = kod.replaceAll("```", "").replace(context.getPrefix() + "eval ", "");
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);

        shell.setVariable("eventWaiter", eventWaiter);
        shell.setVariable("commandManager", commandManager);
        shell.setVariable("context", context);
        shell.setVariable("caseDao", caseDao);
        shell.setVariable("modLog", modLog);
        shell.setVariable("karyJSON", karyJSON);
        shell.setVariable("tlumaczenia", tlumaczenia);
        shell.setVariable("commandExecute", commandExecute);
        shell.setVariable("userDao", userDao);
        shell.setVariable("nieobecnosciDao", nieobecnosciDao);
        shell.setVariable("remindDao", remindDao);
        shell.setVariable("modulManager", modulManager);
        shell.setVariable("giveawayListener", giveawayListener);
        shell.setVariable("giveawayDao", giveawayDao);

        Object value;
        boolean error = false;
        try {
            value = shell.evaluate(kod);
        } catch (Exception e) {
            error = true;
            value = e.getMessage();
        }

        EmbedBuilder eb = new EmbedBuilder();
        if (!error) eb.setColor(Color.green);
        else eb.setColor(Color.red);

        eb.addField("\ud83d\udce4 INPUT", codeBlock("java", kod), false);
        eb.addField("\ud83d\udce5 OUTPUT", codeBlock("java", value), false);
        context.getChannel().sendMessage(eb.build()).queue();
        context.getMember().getVoiceState()
        return true;
    }

    private String codeBlock(String code, Object text) {
        return "```" + code + "\n" + text + "```";
    }


}
