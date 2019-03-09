package io.github.rowak.nanoleafdesktop.shortcuts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;

public class ShortcutManager
{
	public static Shortcut[] getSavedShortcuts()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		Shortcut[] shortcuts = new Shortcut[0];
		String devicesStr = manager.getProperty("shortcuts");
		if (devicesStr != null)
		{
			JSONArray arr = new JSONArray(devicesStr);
			shortcuts = new Shortcut[arr.length()];
			for (int i = 0; i < arr.length(); i++)
			{
				JSONObject json = arr.getJSONObject(i);
				String name = json.getString("name");
				JSONArray keysjson = json.getJSONArray("keys");
				List<String> keys = new ArrayList<String>();
				for (Object o : keysjson)
				{
					keys.add((String)o);
				}
				JSONObject actionjson = json.getJSONObject("action");
				String actionTypeStr = actionjson.getString("type");
				JSONArray argsjson = actionjson.getJSONArray("args");
				Object[] args = null;
				if (argsjson != null)
				{
					args = new Object[argsjson.length()];
					for (int j = 0; j < argsjson.length(); j++)
					{
						args[j] = argsjson.get(j);
					}
				}
				if (name != null && keys != null && actionTypeStr != null)
				{
					ActionType actionType = nameToActionType(actionTypeStr);
					Action action = new Action(actionType, args);
					shortcuts[i] = new Shortcut(name, keys, action);
				}
			}
		}
		return shortcuts;
	}
	
	public static void saveShortcut(Shortcut newShortcut)
	{
		Shortcut[] shortcutsarr = getSavedShortcuts();
		List<Shortcut> shortcuts = new ArrayList<Shortcut>();
		for (Shortcut s : shortcutsarr)
		{
			shortcuts.add(s);
		}
		shortcuts.add(newShortcut);
		saveShortcuts(shortcuts.toArray(new Shortcut[]{}));
	}
	
	public static void removeShortcut(String name)
	{
		Shortcut[] shortcutsarr = getSavedShortcuts();
		List<Shortcut> shortcuts = new ArrayList<Shortcut>();
		for (Shortcut s : shortcutsarr)
		{
			if (!s.getName().equals(name))
			{
				shortcuts.add(s);
			}
		}
		saveShortcuts(shortcuts.toArray(new Shortcut[]{}));
	}
	
	private static void saveShortcuts(Shortcut[] shortcuts)
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		JSONArray jsonarr = new JSONArray();
		for (int i = 0; i < shortcuts.length; i++)
		{
			Shortcut s = shortcuts[i];
			JSONObject json = new JSONObject();
			json.put("name", s.getName());
			json.put("keys", s.getKeys());
			JSONObject actionjson = new JSONObject();
			actionjson.put("type", s.getAction().getType());
			JSONArray argsjson = new JSONArray();
			Object[] args = s.getAction().getArgs();
			if (args != null)
			{
				for (int j = 0; j < args.length; j++)
				{
					argsjson.put(j, args[j]);
				}
			}
			actionjson.put("args", argsjson);
			json.put("action", actionjson);
			jsonarr.put(i, json);
		}
		manager.setProperty("shortcuts", jsonarr.toString());
	}
	
	private static ActionType nameToActionType(String name)
	{
		name = name.toUpperCase().replace(' ', '_');
		for (ActionType type : ActionType.values())
		{
			if (name.equals(type.toString()))
			{
				return type;
			}
		}
		return null;
	}
}
