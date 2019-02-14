package io.github.rowak.nanoleafdesktop.discovery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.rowak.Color;
import io.github.rowak.Effect;

public class EffectMetadata
{
	private String name;
	private String description;
	private String type;
	private String uuid;
	private String[] tags;
	private String creator;
	private String key;
	private int downloads;
	private Color[] palette;
	
	public EffectMetadata(JSONObject data)
	{
		this.name = data.getString("effect_name");
		this.description = data.getString("effect_description");
		this.type = data.getString("effect_type");
		this.uuid = data.getString("uuid");
		this.tags = data.getJSONArray("tags").toList().toArray(new String[]{});
		this.creator = data.getJSONObject("creator").getString("display_name");
		this.key = data.getString("key");
		this.downloads = data.getInt("downloads");
		this.palette = jsonToPalette(data.getJSONArray("palette"));
	}
	
	public Effect getEffect()
	{
		return Discovery.downloadEffect(this.key);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public String getUuid()
	{
		return this.uuid;
	}
	
	public String[] getTags()
	{
		return this.tags;
	}
	
	public String getCreator()
	{
		return this.creator;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public int getDownloads()
	{
		return this.downloads;
	}
	
	public Color[] getPalette()
	{
		return this.palette;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	private Color[] jsonToPalette(JSONArray arr)
	{
		Color[] palette = new Color[arr.length()];
		for (int i = 0; i < arr.length(); i++)
		{
			JSONObject colors = arr.getJSONObject(i);
			int hue = colors.getInt("hue");
			int sat = colors.getInt("saturation");
			int brightness = colors.getInt("brightness");
			palette[i] = Color.fromHSB(hue,
					sat, brightness);
			try
			{
				double probability = colors.getDouble("probability");
				palette[i].setProbability(probability);
			}
			catch (JSONException je)
			{
				palette[i].setProbability(-1);
			}
		}
		return palette;
	}
}
