package pl.kamil0024.commands.system;

import com.google.inject.Inject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.VoiceChannel;
import pl.kamil0024.bdate.BDate;
import pl.kamil0024.commands.ModLog;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.module.ModulManager;
import pl.kamil0024.core.musicapi.MusicAPI;
import pl.kamil0024.core.util.Statyczne;
import pl.kamil0024.core.util.UserUtil;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class BotinfoCommand extends Command {

    @Inject private final CommandManager commandManager;
    @Inject private final ModulManager modulManager;
    @Inject private final MusicAPI musicAPI;

    public BotinfoCommand(CommandManager commandManager, ModulManager modulManager, MusicAPI musicAPI) {
        name = "botinfo";
        aliases = Arrays.asList("botstat", "botstats");
        cooldown = 5;

        this.commandManager = commandManager;
        this.modulManager = modulManager;
        this.musicAPI = musicAPI;
    }

    @Override
    public boolean execute(CommandContext context) {
        EmbedBuilder eb = new EmbedBuilder();
        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();

        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        double used = round((double) (total - free) / 1024 / 1024);
        String format = String.format("%s/%s MB", used, round((double) total / 1024 / 1024));

        long startCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        long start = System.nanoTime();
        int cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.ram"), format, false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.cpu"), calcCPU(startCPUTime, start, cpuCount) + "%", false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.uptime"), new BDate(Statyczne.START_DATE.getTime(), ModLog.getLang()).difference(new Date().getTime()), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.jda"), JDAInfo.VERSION, false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.shard"), String.format("[ %s / %s ]", context.getJDA().getShardInfo().getShardId(), context.getJDA().getShardInfo().getShardTotal()), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.core"), Statyczne.WERSJA, false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.jre"), System.getProperty("java.version"), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.os"), System.getProperty("os.name"), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.users"), String.valueOf(context.getGuild().getMemberCount()),
                false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.name"), UserUtil.getFullName(context.getBot()), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.cmd"), String.valueOf(commandManager.getCommands().size()), false));
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.modules"), String.valueOf(modulManager.getModules().size()), false));

        int polaczonych = 0;
        for (Integer port : musicAPI.getPorts()) {
            VoiceChannel vc = musicAPI.getAction(port).getVoiceChannel();
            if (vc != null) {
                polaczonych++;
            }
        }
        fields.add(new MessageEmbed.Field(context.getTranslate("botinfo.musicbots"), String.format("[ %s / %s ]", polaczonych, musicAPI.getPorts().size()), false));

        eb.setColor(UserUtil.getColor(context.getMember()));

        int i = 1;
        for (MessageEmbed.Field field : fields) {
            boolean bol = true;
            if (i > 3) {
                i = 0;
                bol = false;
            }
            eb.addField(field.getName(), field.getValue(), bol);
            i++;
        }
        context.send(eb.build()).queue();
        return true;
    }

    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int calcCPU(long cpuStartTime, long elapsedStartTime, int cpuCount) {
        long end = System.nanoTime();
        long totalAvailCPUTime = cpuCount * (end-elapsedStartTime);
        long totalUsedCPUTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()-cpuStartTime;
        float per = ((float)totalUsedCPUTime*100)/(float)totalAvailCPUTime;
        return (int) per;
    }


}
