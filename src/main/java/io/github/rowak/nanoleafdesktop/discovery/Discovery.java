package io.github.rowak.nanoleafdesktop.discovery;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.util.HttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Discovery {
	
    private static final String BASE_ENDPOINT = "https://my.nanoleaf.me/api/v1";

    public static EffectMetadata[] getTopEffects(int page, List<String> tags) {
        JSONObject topEffects = getEffectsData("top", page, tags);
        return getEffectsList(topEffects);
    }

    public static EffectMetadata[] getRecentEffects(int page, List<String> tags) {
        JSONObject recentEffects = getEffectsData("recent", page, tags);
        return getEffectsList(recentEffects);
    }

    public static EffectMetadata[] getEffectsByType(String type,
                                                    int page, List<String> tags) {
        JSONObject effectsData = getEffectsData(type, page, tags);
        return getEffectsList(effectsData);
    }

    public static JSONObject getEffectsData(String type, int page, List<String> tags) {
        JSONObject json = null;
        while (json == null) {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                		.connectTimeout(20, TimeUnit.SECONDS)
                		.readTimeout(20, TimeUnit.SECONDS)
                		.build();
                String url = String.format(
                        "%s/effects/%s?page=%d&tags=%s", BASE_ENDPOINT,
                        type, page, tagsToString(tags));
                Response resp = HttpUtil.getHttpSync(client, url);
                return new JSONObject(resp.body().string());
            }
            catch (JSONException je) {
                // do nothing, get json again
            }
            catch (IOException e) {
            	e.printStackTrace();
            }
        }
        return null;
    }

    private static EffectMetadata[] getEffectsList(JSONObject data) {
        if (data == null) {
            return new EffectMetadata[0];
        }

        int numItems = data.getInt("items");
        JSONArray items = data.getJSONArray("data");
        EffectMetadata[] effects = new EffectMetadata[numItems];

        for (int i = 0; i < numItems; i++) {
            effects[i] = new EffectMetadata(items.getJSONObject(i));
        }

        return effects;
    }

    public static Effect downloadEffect(String key) {
    	try {
	        OkHttpClient client = new OkHttpClient.Builder()
	        		.connectTimeout(10, TimeUnit.SECONDS)
	        		.readTimeout(10, TimeUnit.SECONDS)
	        		.build();
	        String url = String.format("%s/effects/download/%s",
	                BASE_ENDPOINT, key);
	        Response resp = HttpUtil.getHttpSync(client, url);
	        return Effect.createFromJSON(new JSONObject(resp.body().string()));
    	}
    	catch (IOException e) {
    		return null;
    	}
    }

    private static String tagsToString(List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag);
            if (tags.indexOf(tag) < tags.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString().toLowerCase();
    }
}
