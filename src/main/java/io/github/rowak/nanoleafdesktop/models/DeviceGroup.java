package io.github.rowak.nanoleafdesktop.models;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceGroup
{
	private String name;
	private DeviceInfo[] devices;
	
	public DeviceGroup(String name, DeviceInfo[] devices)
	{
		this.name = name;
		this.devices = devices;
	}
	
	public static DeviceGroup fromJSON(String json)
	{
		JSONObject obj = new JSONObject(json);
		JSONArray arr = obj.getJSONArray("devices");
		DeviceInfo[] devices = new DeviceInfo[arr.length()];
		for (int i = 0; i < devices.length; i++)
		{
			devices[i] = DeviceInfo.fromJSON(arr.getJSONObject(i).toString());
		}
		return new DeviceGroup(obj.getString("name"), devices);
	}
	
	public String getName()
	{
		return name;
	}
	
	public DeviceInfo[] getDevices()
	{
		return devices;
	}
	
	public String toJSON()
	{
		return new JSONObject(this).toString();
	}
}
