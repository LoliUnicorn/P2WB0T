package pl.kamil0024.musicbot.core.util;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class GsonUtil {

    private GsonUtil() {}

    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().disableHtmlEscaping().create();
    }

    public static String toJSON(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJSON(byte[] data, Class<T> object) {
        return GSON.fromJson(new String(data, Charsets.UTF_8), object);
    }

    public static <T> T fromJSON(byte[] data, TypeToken<T> object) {
        return GSON.fromJson(new String(data, Charsets.UTF_8), object.getType());
    }

    public static <T> T fromJSON(String text, Class<T> object) {
        return GSON.fromJson(text, object);
    }

    public static <T> T fromJSON(String text, TypeToken<T> object) {
        return GSON.fromJson(text, object.getType());
    }

    public static <T> T fromJSON(String data, Type type) {
        return GSON.fromJson(data, type);
    }
}
