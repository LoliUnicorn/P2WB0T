package pl.kamil0024.core.util;

public class ElseGet {

    public static Object get(Object value, Object orElseGet) {
        if (value == null) return orElseGet;
        return value;
    }

}
