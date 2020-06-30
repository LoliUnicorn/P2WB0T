package pl.kamil0024.core.arguments;

import lombok.Getter;
import pl.kamil0024.core.logger.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArgumentManager {

    @Getter public Map<String, Args> arguments;

    public ArgumentManager() {
            arguments = new HashMap<>();
        }

    public void registerAll() {
        ArrayList<Args> args = new ArrayList<>();

        args.add(new UserArgument());
        args.add(new MemberArgument());
        args.add(new TextChannelArgument());

        args.forEach(this::register);
    }

    public void register(Args arg) {
        if (arg == null) return;
        if (arguments.containsKey(arg.toString())) throw new IllegalArgumentException("Ten argument jest juz zarejestrowany!");
        arguments.put(arg.toString(), arg);
        Log.debug("Rejestruje argument '%s'", arg.getName());
    }


    public Args getArgument(String name) {
        return getArguments().get(name);
    }
}
