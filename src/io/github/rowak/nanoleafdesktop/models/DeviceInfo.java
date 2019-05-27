package io.github.rowak.nanoleafdesktop.models;

import org.json.JSONObject;

public class DeviceInfo
{
	private String hostName;
	private int port;
	private String accessToken;
	
	public DeviceInfo(String hostName, int port, String accessToken)
	{
		this.hostName = hostName;
		this.port = port;
		this.accessToken = accessToken;
	}
	
	public static DeviceInfo fromJSON(String json)
	{
		JSONObject obj = new JSONObject(json);
		return new DeviceInfo(obj.getString("hostName"),
				obj.getInt("port"), obj.getString("accessToken"));
	}
	
	public String getHostName()
	{
		return hostName;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String getAccessToken()
	{
		return accessToken;
	}
	
	public String toJSON()
	{
		return new JSONObject(this).toString();
	}
}
