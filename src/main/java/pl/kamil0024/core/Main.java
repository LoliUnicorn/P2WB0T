package pl.kamil0024.core;

import pl.kamil0024.core.logger.Log;

public class Main {

    private static Main instance;

    public static void main(String[] args) {
        Log.info("Startuje...");

        if (args.length != 1) {
            Log.error("Uzycie: <bot token>");
            System.exit(1);
        }
        new B0T(args[0]);
    }

    public static Class<? extends Main> getMain() {
        return instance.getClass();
    }

}
