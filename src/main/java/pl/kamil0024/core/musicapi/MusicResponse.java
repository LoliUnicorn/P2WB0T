package pl.kamil0024.core.musicapi;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import pl.kamil0024.api.Response;

public class MusicResponse {

    private final JSONObject json;

    public MusicResponse(JSONObject json) {
        this.json = json;
    }

    public boolean isError() {
        return json.getBoolean("succes");
    }

    @Nullable
    public Response.Error getError() {
        if (!isError()) return null;
        return new Response.Error(json.getString("body"), json.getString("description"));
    }



}
