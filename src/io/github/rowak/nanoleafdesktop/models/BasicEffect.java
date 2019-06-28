package io.github.rowak.nanoleafdesktop.models;

public class BasicEffect
{
	private String name;
	private int hue, sat;
	
	public BasicEffect(String name, int hue, int sat)
	{
		this.name = name;
		this.hue = hue;
		this.sat = sat;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public int getHue()
	{
		return hue;
	}
	
	public void setHue(int hue)
	{
		this.hue = hue;
	}
	
	public int getSaturation()
	{
		return sat;
	}
	
	public void setSaturation(int sat)
	{
		this.sat = sat;
	}
}
