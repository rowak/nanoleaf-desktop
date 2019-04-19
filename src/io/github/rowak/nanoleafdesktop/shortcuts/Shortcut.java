package io.github.rowak.nanoleafdesktop.shortcuts;

import java.util.Arrays;
import java.util.List;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;

public class Shortcut
{
	private String name;
	private List<String> keys;
	private RunType runType;
	private Action action;
	
	public Shortcut(String name, List<String> keys,
			RunType runType, Action action)
	{
		this.name = name;
		this.keys = keys;
		this.runType = runType;
		this.action = action;
	}
	
	public void execute(Aurora device, Effect[] effects)
	{
		action.execute(device, effects);
	}
	
	public void reset(Aurora device, Effect[] effects)
	{
		action.reset(device, effects);
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
	
	public RunType getRunType()
	{
		return runType;
	}
	
	public void setRunType(RunType type)
	{
		this.runType = type;
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
		if (runType == RunType.WHEN_PRESSED || runType == RunType.WHILE_HELD)
		{
			String keysStr = keys.get(0);
			for (int i = 1; i < keys.size(); i++)
			{
				keysStr += " + " + keys.get(i);
			}
			return String.format("Name: %s    Event: %s    Trigger: %s    Run: %s    Args: %s", name,
					action.getType(), keysStr, runType, Arrays.asList(action.getArgs()));
		}
		else
		{
			String appName = (String)action.getArgs()[1];
			return String.format("Name: %s    Event: %s    Trigger: %s    Run: %s    Args: %s", name,
					action.getType(), appName, runType, Arrays.asList(action.getArgs()));
		}
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Shortcut)
		{
			Shortcut o = (Shortcut)other;
			return this.name.equals(o.name) &&
					this.keys.equals(o.keys) &&
					this.action.getType().equals(o.action.getType()) &&
					this.runType.equals(o.runType) &&
					Arrays.asList(this.action.getArgs()).equals(
							Arrays.asList(o.action.getArgs()));
		}
		return false;
	}
}
