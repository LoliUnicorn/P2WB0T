package pl.kamil0024.core.musicapi;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pl.kamil0024.api.Response;

public class MusicResponse {

    public final JSONObject json;

    public MusicResponse(JSONObject json) {
        this.json = json;
    }

    public boolean isError() {
        try {
            return !json.getBoolean("succes");
        } catch (Exception e) {
            return !json.getJSONObject("map").getBoolean("succes");
        }
    }

    @Nullable
    public Response.Error getError() {
        if (!isError()) return null;
        JSONObject tak = json.getJSONObject("error");
        return new Response.Error(tak.getString("body"), tak.getString("description"));
    }

    @Override
    public String toString() {
        return new Gson().toJson(json);
    }

}
