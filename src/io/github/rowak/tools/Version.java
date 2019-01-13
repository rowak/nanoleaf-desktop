package io.github.rowak.tools;

import org.json.JSONObject;

/**
 * A local version interface for the GitHub REST api.
 */
public class Version
{
	int semVer;
	boolean preRelease;
	String name;
	
	public Version(String name, boolean preRelease)
	{
		parseVersion(name);
		this.name = name;
		this.preRelease = preRelease;
	}
	
	public Version(JSONObject json)
	{
		parseVersion(json.getString("name"));
		name = json.getString("name");
		preRelease = json.getBoolean("prerelease");
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public boolean getPreRelease()
	{
		return this.preRelease;
	}
	
	private void parseVersion(String rawVersion)
	{
		rawVersion = rawVersion.replace("v", "");
		String[] semVerArr = rawVersion.split("\\.");
		String semVerStr = "";
		for (int i = 0; i < semVerArr.length; i++)
		{
			semVerStr += semVerArr[i];
		}
		semVer = Integer.parseInt(semVerStr);
	}
	
	public boolean greater(Version other)
	{
		return this.semVer > other.semVer ||
				(this.semVer == other.semVer && this.preRelease && !other.preRelease);
	}
}
