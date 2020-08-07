package pl.kamil0024.musicbot.core.util;

import lombok.Getter;
import org.json.JSONObject;

public class JSONResponse extends JSONObject {
    @Getter private final int code;
    public JSONResponse(String string, int code) {
        super(string);
        this.code = code;
    }
}