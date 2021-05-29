package io.github.rowak.nanoleafdesktop.shortcuts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafCallback;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.PluginEffect;
import io.github.rowak.nanoleafapi.StaticEffect;

public class Action {
	
	private ActionType type;
	private Object[] args;
	private Map<NanoleafDevice, Object> states;
	
	public Action(ActionType type, Object[] args) {
		this.type = type;
		this.args = args;
		states = new HashMap<NanoleafDevice, Object>();
	}
	
	public Action(String type, String mode, String data) {
		this.type = getType(type, getMode(mode));
		if (type == null) {
			throw new IllegalArgumentException("Invalid action type");
		}
		if (type.equalsIgnoreCase("brightness") || type.equalsIgnoreCase("temp") ||
				type.equalsIgnoreCase("red") || type.equalsIgnoreCase("green") ||
				type.equalsIgnoreCase("blue") || type.equalsIgnoreCase("hue") ||
				type.equalsIgnoreCase("saturation"))
		{
			try {
				this.args = new Object[] {Integer.parseInt(data)};
			}
			catch (NumberFormatException e) {
				System.out.println("Invalid value type");
			}
		}
		else {
			this.args = new Object[] {data};
		}
	}
	
	private ModeType getMode(String mode) {
		if (mode == null)
			return null;
		switch (mode.toLowerCase()) {
			case "up": return ModeType.UP;
			case "down": return ModeType.DOWN;
			case "set": return ModeType.SET;
			default: return ModeType.NO_MODE;
		}
	}
	
	private ActionType getType(String type, ModeType mode) {
		if (type.equalsIgnoreCase("on")) {
			return ActionType.DEVICE_ON;
		}
		else if (type.equalsIgnoreCase("off")) {
			return ActionType.DEVICE_OFF;
		}
		else if (type.equalsIgnoreCase("toggle")) {
			return ActionType.DEVICE_TOGGLE;
		}
		else if (type.equalsIgnoreCase("brightness")) {
			if (mode == ModeType.UP) return ActionType.INCREASE_BRIGHTNESS;
			else if (mode == ModeType.DOWN) return ActionType.DECREASE_BRIGHTNESS;
			else if (mode == ModeType.SET) return ActionType.SET_BRIGHTNESS;
		}
		else if (type.equalsIgnoreCase("temp")) {
			if (mode == ModeType.UP) return ActionType.INCREASE_COLOR_TEMP;
			else if (mode == ModeType.DOWN) return ActionType.DECREASE_COLOR_TEMP;
			else if (mode == ModeType.SET) return ActionType.SET_COLOR_TEMP;
		}
		else if (type.equalsIgnoreCase("effect")) {
			return ActionType.SET_EFFECT;
		}
		else if (type.equalsIgnoreCase("hue")) {
			return ActionType.SET_HUE;
		}
		else if (type.equalsIgnoreCase("saturation")) {
			return ActionType.SET_SATURATION;
		}
		else if (type.equalsIgnoreCase("red")) {
			return ActionType.SET_RED;
		}
		else if (type.equalsIgnoreCase("green")) {
			return ActionType.SET_GREEN;
		}
		else if (type.equalsIgnoreCase("blue")) {
			return ActionType.SET_BLUE;
		}
		else if (type.equalsIgnoreCase("rgb")) {
			return ActionType.SET_RGB;
		}
		else if (type.equalsIgnoreCase("hsb")) {
			return ActionType.SET_HSB;
		}
		return null;
	}
	
	public ActionType getType() {
		return type;
	}
	
	public void setType(ActionType type) {
		this.type = type;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	public void setArgs(Object[] args) {
		this.args = args;
	}
	
//	public Object getPreviousState()
//	{
//		return previousState;
//	}
	
	public Map<NanoleafDevice, Object> getStates() {
		return states;
	}
	
	public void execute(NanoleafGroup group, Effect[] effects) {
		if (type == null)
			return;
		
		try {
			switch (type) {
				case DEVICE_ON:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getOn());
						device.setOnAsync(true, new ActionCallback<String>());
					}
					break;
				case DEVICE_OFF:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getOn());
						device.setOnAsync(false, new ActionCallback<String>());
					}
					break;
				case DEVICE_TOGGLE:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getOn());
						device.toggleOnAsync(new ActionCallback<Boolean>());
					}
					break;
				case INCREASE_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getBrightness());
						device.increaseBrightnessAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case DECREASE_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getBrightness());
						device.decreaseBrightnessAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case SET_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getOn());
						device.setBrightnessAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case INCREASE_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getColorTemperature());
						device.increaseColorTemperatureAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case DECREASE_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getColorTemperature());
						device.decreaseColorTemperatureAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case SET_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getColorTemperature());
						device.setColorTemperatureAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case SET_HUE:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getHue());
						device.setHueAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case SET_SATURATION:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getSaturation());
						device.setSaturationAsync((int)args[0], new ActionCallback<String>());
					}
					break;
				case SET_EFFECT:
					for (NanoleafDevice device : group.getDevices().values()) {
						states.put(device, device.getCurrentEffectName());
						device.setEffectAsync((String)args[0], new ActionCallback<String>());
					}
					break;
				case NEXT_EFFECT:
					group.forEach((device) -> {
						try {
							List<String> effectNames = device.getEffectsList();
							String currentEffect = device.getCurrentEffectName();
							states.put(device, currentEffect);
							int nextEffect = -1;
							for (int i = 0; i < effects.length; i++) {
								if (currentEffect.equals(effects[i].getName()) && i+1 < effects.length) {
									if (!isRhythmEffect(effects[i+1])) {
										nextEffect = i+1;
									}
									else {
										int j = i+1;
										while (nextEffect == -1 && j < effects.length) {
											if (!isRhythmEffect(effects[j])) {
												nextEffect = j;
												break;
											}
											j++;
										}
									}
									break;
								}
							}
							if (nextEffect == -1) {
								device.setEffectAsync(effectNames.get(0), new ActionCallback<String>());
							}
							else {
								device.setEffectAsync(effectNames.get(nextEffect), new ActionCallback<String>());
							}
						}
						catch (NanoleafException | IOException e) {
							
						}
					});
					break;
				case PREVIOUS_EFFECT:
					group.forEach((device) -> {
						try {
							List<String> effectNames = device.getEffectsList();
							String currentEffect = device.getCurrentEffectName();
							states.put(device, currentEffect);
							int nextEffect = -1;
							for (int i = 0; i < effects.length; i++) {
								if (currentEffect.equals(effects[i].getName()) && i-1 > -1) {
									if (!isRhythmEffect(effects[i-1])) {
										nextEffect = i-1;
									}
									else {
										int j = i-1;
										while (nextEffect == -1 && j > -1) {
											if (!isRhythmEffect(effects[j])) {
												nextEffect = j;
												break;
											}
											j--;
										}
									}
									break;
								}
							}
							if (nextEffect == -1) {
								device.setEffectAsync(effectNames.get(effectNames.size()-1), new ActionCallback<String>());
							}
							else {
								device.setEffectAsync(effectNames.get(nextEffect), new ActionCallback<String>());
							}
						}
						catch (NanoleafException | IOException e) {
							
						}
					});
					break;
				case SET_RED:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = device.getColor();
						states.put(device, device.getOn());
						device.setColor(Color.fromRGB((int)args[0], c.getGreen(), c.getBlue()));
					}
					break;
				case SET_GREEN:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = device.getColor();
						states.put(device, device.getOn());
						device.setColor(Color.fromRGB(c.getRed(), (int)args[0], c.getBlue()));
					}
					break;
				case SET_BLUE:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = device.getColor();
						states.put(device, device.getOn());
						device.setColor(Color.fromRGB(c.getRed(), c.getGreen(), (int)args[0]));
					}
					break;
				case SET_RGB:
					int[] cmp = getColorComponents((String)args[0]);
					if (cmp != null) {
						Color rgb = Color.fromRGB(cmp[0], cmp[1], cmp[2]);
						Frame frame = new Frame(rgb, 5);
						for (NanoleafDevice device : group.getDevices().values()) {
							states.put(device, device.getColor());
							Effect color = new StaticEffect.Builder(device)
									.setAllPanels(frame).build("");
							device.displayEffectAsync(color, new ActionCallback<String>());
						}
					}
					break;
				case SET_HSB:
					cmp = getColorComponents((String)args[0]);
					if (cmp != null) {
						Color hsb = Color.fromHSB(cmp[0], cmp[1], cmp[2]);
						Frame frame = new Frame(hsb, 5);
						for (NanoleafDevice device : group.getDevices().values()) {
							states.put(device, device.getColor());
							Effect color = new StaticEffect.Builder(device)
									.setAllPanels(frame).build("");
							device.displayEffectAsync(color, new ActionCallback<String>());
						}
					}
					break;
			}
		}
		catch (NanoleafException | IOException e) {
//			sce.printStackTrace();
		}
	}
	
	private int[] getColorComponents(String str) {
		int[] cmp = new int[3];
		try {
			int offset = str.length() - 6;
			// x,y,z
			if (str.contains(",")) {
				String[] cmpstr = str.split(",");
				for (int i = 0; i < cmpstr.length; i++) {
					cmp[i] = Integer.parseInt(cmpstr[i]);
				}
			}
			// #XXYYZZ or XXYYZZ
			else if (offset == 0 || offset == 1) {
				cmp[0] = hexToInt(str.substring(offset, 2+offset));
				cmp[1] = hexToInt(str.substring(2+offset, 4+offset));
				cmp[2] = hexToInt(str.substring(4+offset, 6+offset));
			}
			return cmp;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	private int hexToInt(String hex) {
		int n = 0;
		char c;
		for (int i = 0; i < hex.length(); i++) {
			c = hex.charAt(i);
			if (c >= '0' && c <= '9') {
				n += (c-'0')*Math.pow(10, (hex.length()-i-1));
			}
			else if (c >= 'A' && c <= 'Z') {
				n += (c-'A'+10)*Math.pow(16, (hex.length()-i-1));
			}
			else if (c >= 'a' && c <= 'z') {
				n += (c-'a'+10)*Math.pow(16, (hex.length()-i-1));
			}
		}
		return n;
	}
	
	public void reset(NanoleafGroup group, Effect[] effects) {
		try {
			switch (type) {
				case DEVICE_ON:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setOnAsync((boolean)states.get(device), new ActionCallback<String>());
					}
					break;
				case DEVICE_OFF:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setOnAsync((boolean)states.get(device), new ActionCallback<String>());
					}
					break;
				case DEVICE_TOGGLE:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setOnAsync((boolean)states.get(device),new ActionCallback<String>());
					}
					break;
				case INCREASE_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.increaseBrightnessAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case DECREASE_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.decreaseBrightnessAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_BRIGHTNESS:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setBrightnessAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case INCREASE_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setColorTemperatureAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case DECREASE_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setColorTemperatureAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_COLOR_TEMP:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setColorTemperatureAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_HUE:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setHueAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_SATURATION:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setSaturationAsync((int)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_EFFECT:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setEffectAsync((String)states.get(device), new ActionCallback<String>());
					}
					break;
				case NEXT_EFFECT:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setEffectAsync((String)states.get(device), new ActionCallback<String>());
					}
					break;
				case PREVIOUS_EFFECT:
					for (NanoleafDevice device : group.getDevices().values()) {
						device.setEffectAsync((String)states.get(device), new ActionCallback<String>());
					}
					break;
				case SET_RED:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = (Color)states.get(device);
						device.setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
				case SET_GREEN:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = (Color)states.get(device);
						device.setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
				case SET_BLUE:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = (Color)states.get(device);
						device.setColor(Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));
					}
					break;
				case SET_RGB:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = (Color)states.get(device);
						device.setColor(c);
					}
					break;
				case SET_HSB:
					for (NanoleafDevice device : group.getDevices().values()) {
						Color c = (Color)states.get(device);
						device.setColor(c);
					}
					break;
			}
			states.clear();
		}
		catch (NanoleafException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isRhythmEffect(Effect ef) {
		return ef.getEffectType() == "plugin" && ((PluginEffect)ef).getPlugin().getType().equals("rhythm");
	}
	
	private class ActionCallback<T> implements NanoleafCallback<T> {

		@Override
		public void onCompleted(int status, Object data, NanoleafDevice device) {
			if (status != NanoleafCallback.SUCCESS) {
				// TODO: handle error
			}
		}
	}
}
