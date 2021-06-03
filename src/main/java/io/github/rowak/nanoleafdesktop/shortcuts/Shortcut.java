package io.github.rowak.nanoleafdesktop.shortcuts;

import java.util.Arrays;
import java.util.List;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.NanoleafGroup;

public class Shortcut {
	
	private String name;
	private List<String> keys;
	private RunType runType;
	private Action action;
	
	public Shortcut(String name, List<String> keys,
			RunType runType, Action action) {
		this.name = name;
		this.keys = keys;
		this.runType = runType;
		this.action = action;
	}
	
	public void execute(NanoleafGroup group, Effect[] effects) {
		action.execute(group, effects);
	}
	
	public void reset(NanoleafGroup group, Effect[] effects) {
		action.reset(group, effects);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getKeys() {
		return keys;
	}
	
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
	
	public RunType getRunType() {
		return runType;
	}
	
	public void setRunType(RunType type) {
		this.runType = type;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	private String getActionString(Object arg) {
		switch (action.getType()) {
			case DEVICE_ON: return "Turn on";
			case DEVICE_OFF: return "Turn off";
			case DEVICE_TOGGLE: return "Toggle on";
			case SET_BRIGHTNESS: return "Set brightness to " + arg + "%";
			case INCREASE_BRIGHTNESS: return "Increase brightness by " + arg + "%";
			case DECREASE_BRIGHTNESS: return "Decrease brightness by " + arg + "%";
			case SET_COLOR_TEMP: return "Set color temperature to " + arg + "K";
			case INCREASE_COLOR_TEMP: return "Increase color temperature by " + arg + "K";
			case DECREASE_COLOR_TEMP: return "Decrease color temperature by " + arg + "K";
			case SET_EFFECT: return "Set effect to \"" + arg + "\"";
			case NEXT_EFFECT: return "Switch to next effect";
			case PREVIOUS_EFFECT: return "Switch to last effect";
			case SET_HUE: return "Set hue to " + arg;
			case SET_SATURATION: return "Set saturation to " + arg;
			case SET_RED: return "Set RGB red value to " + arg;
			case SET_GREEN: return "Set RGB green value to " + arg;
			case SET_BLUE: return "Set RGB blue value to " + arg;
			case SET_RGB: return "Set RGB color to " + arg;
			case SET_HSB: return "Set HSB color to " + arg;
			default: return "";
		}
	}
	
	@Override
	public String toString() {
		Object arg = action.getArgs().length > 0 ? action.getArgs()[0] : null;
		if (runType == RunType.WHEN_PRESSED || runType == RunType.WHILE_HELD) {
			String keysStr = keys.get(0);
			for (int i = 1; i < keys.size(); i++) {
				keysStr += " + " + keys.get(i);
			}
			String run = runType == RunType.WHEN_PRESSED ? "when pressing" : "while holding";
			return String.format("%s (%s %s \"%s\")", name,
					getActionString(arg), run, keysStr);
		}
		else {
			String run = runType == RunType.WHEN_APP_RUN ? "when running" : "when closing";
			String appName = (String)action.getArgs()[1];
			return String.format("%s (%s %s %s)", name,
					getActionString(arg), run, appName);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Shortcut) {
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
