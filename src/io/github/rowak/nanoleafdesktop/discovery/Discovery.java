package io.github.rowak.nanoleafdesktop.discovery;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest;

import io.github.rowak.Effect;

public class Discovery
{
	private static final String BASE_ENDPOINT = "https://my.nanoleaf.me/api/v1";
	
	public static EffectMetadata[] getTopEffects(int page, List<String> tags)
	{
		return getEffectsList(getEffectsData("top", page, tags));
	}
	
	public static EffectMetadata[] getRecentEffects(int page, List<String> tags)
	{
		return getEffectsList(getEffectsData("recent", page, tags));
	}
	
	public static EffectMetadata[] getEffectsByType(String type,
			int page, List<String> tags)
	{
		return getEffectsList(getEffectsData(type, page, tags));
	}
	
	public static JSONObject getEffectsData(String type, int page, List<String> tags)
	{
		JSONObject json = null;
		while (json == null)
		{
			try
			{
				HttpRequest request = HttpRequest.get(String.format(
						"%s/effects/%s?page=%d&tags=%s", BASE_ENDPOINT,
						type, page, tagsToString(tags)));
				System.out.println(String.format(
						"%s/effects/%s?page=%d&tags=%s", BASE_ENDPOINT,
						type, page, tagsToString(tags)));
				request.connectTimeout(20000);
				request.readTimeout(20000);
				return new JSONObject(request.body());
			}
			catch (JSONException je)
			{
				// do nothing, get json again
			}
		}
		return null;
	}
	
	private static EffectMetadata[] getEffectsList(JSONObject data)
	{
		int numItems = data.getInt("items");
		JSONArray items = data.getJSONArray("data");
		EffectMetadata[] effects = new EffectMetadata[numItems];
		for (int i = 0; i < numItems; i++)
		{
			effects[i] = new EffectMetadata(items.getJSONObject(i));
		}
		return effects;
	}
	
	public static Effect downloadEffect(String key)
	{
		HttpRequest request = HttpRequest.get(String.format("%s/effects/download/%s",
				BASE_ENDPOINT, key));
		request.connectTimeout(10000);
		request.readTimeout(10000);
		return Effect.fromJSON(request.body());
	}
	
	private static String tagsToString(List<String> tags)
	{
		StringBuilder sb = new StringBuilder();
		for (String tag : tags)
		{
			sb.append(tag);
			if (tags.indexOf(tag) < tags.size()-1)
			{
				sb.append(",");
			}
		}
		return sb.toString().toLowerCase();
	}
}
