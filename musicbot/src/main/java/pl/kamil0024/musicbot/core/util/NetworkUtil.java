package pl.kamil0024.musicbot.core.util;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NetworkUtil {
    private NetworkUtil() {}

    private static final String USER_AGENT = "P2WB0T";
    private static final String UA = "User-Agent";
    private static final String AUTH = "Authorization";
    @Getter private static final OkHttpClient client = new OkHttpClient();

    public static JSONResponse getJson(String url) throws IOException {
        Request req = new Request.Builder()
                .header(UA, USER_AGENT)
                .url(url)
                .build();
        Response res = client.newCall(req).execute();
        return res.body() == null ? null : new JSONResponse(res.body().string(), res.code());
    }

    public static JSONArray getJsonArray(String url) throws IOException {
        Request req = new Request.Builder()
                .header(UA, USER_AGENT)
                .url(url)
                .build();
        Response res = client.newCall(req).execute();
        return res.body() == null ? null : new JSONArray(res.body().string());
    }

    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        }

        catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private static Response downloadResponse(String url) throws IOException {
        Request req = new Request.Builder()
                .header(UA, USER_AGENT)
                .url(url)
                .build();
        return client.newCall(req).execute();
    }

    public static byte[] download(String url) throws IOException {
        Response res = downloadResponse(url);
        return res.body() == null ? new byte[0] : res.body().bytes();
    }

}
