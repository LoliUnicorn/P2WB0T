package pl.kamil0024.core.logger;

import pl.kamil0024.core.util.WebhookUtil;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@SuppressWarnings("unused")
public class Log {


    public Log() { }

    public static void newError(String msg, @Nullable Object... args) {
        WebhookUtil web = new WebhookUtil();
        web.setMessage(String.format(msg, args));
        web.setType(WebhookUtil.LogType.ERROR);
        web.send();

        error(msg, args);
    }

    public static void newError(Throwable e) {
        StringWriter er = new StringWriter();
        e.getCause().printStackTrace(new PrintWriter(er));

        List<String> pages = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("```");
        for (String s : er.toString().split("\n")) {
            sb.append(s);
            if (s.length() >= 1800) {
                sb.append("```");
                pages.add(sb.toString());
                sb = new StringBuilder();
                sb.append("```");
            }
        }

        for (String page : pages) {
            Log.newError(page);
        }

    }

    public static void info(String msg, @Nullable Object... args) {
        log("INFO", msg, args);
    }

    public static void warn(String msg, @Nullable Object... args) {
        log("WARN", msg, args);
    }

    public static void error(String msg, @Nullable Object... args) {
        log("ERROR", msg, args);
    }

    public static void debug(String msg, @Nullable Object... args) {
        log("DEBUG", msg, args);
    }

    private static void log(String type, String msg, @Nullable Object... args) {
        String time = new SimpleDateFormat("dd.MM HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println(String.format("[%s] [%s] %s", time, type, String.format(msg, args)));
    }

}
