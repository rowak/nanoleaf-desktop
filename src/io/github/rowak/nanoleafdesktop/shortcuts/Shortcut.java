package io.github.rowak.nanoleafdesktop.shortcuts;

import java.util.Arrays;
import java.util.List;

import io.github.rowak.Aurora;

public class Shortcut
{
	private String name;
	private List<String> keys;
	private Action action;
	
	public Shortcut(String name, List<String> keys, Action action)
	{
		this.name = name;
		this.keys = keys;
		this.action = action;
	}
	
	public void execute(Aurora device)
	{
		action.execute(device);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public List<String> getKeys()
	{
		return keys;
	}
	
	public void setKeys(List<String> keys)
	{
		this.keys = keys;
	}
	
	public Action getAction()
	{
		return action;
	}
	
	public void setAction(Action action)
	{
		this.action = action;
	}
	
	@Override
	public String toString()
	{
		String keysStr = keys.get(0);
		if (keys.size() > 1)
		{
			keysStr += " + " + keys.get(1);
		}
		return String.format("Name: %s    Event: %s    Trigger: %s    Args: %s", name,
				action.getType(), keysStr, Arrays.asList(action.getArgs()));
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Shortcut)
		{
			Shortcut o = (Shortcut)other;
			return this.getName().equals(o.getName()) &&
					this.getKeys().equals(o.getKeys()) &&
					this.getAction().getType().equals(o.getAction().getType()) &&
					Arrays.asList(this.getAction().getArgs()).equals(
							Arrays.asList(o.getAction().getArgs()));
		}
		return false;
	}
}
