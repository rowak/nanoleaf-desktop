package io.github.rowak.nanoleafdesktop.spotify;

public class UserOption
{
	private String name;
	private String[] options;
	private String value;
	
	public UserOption(String name, String[] options)
	{
		this.name = name;
		this.options = options;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String[] getOptions()
	{
		return options;
	}
	
	public void setOptions(String[] options)
	{
		this.options = options;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
}
