package io.github.rowak.nanoleafdesktop;

import java.io.IOException;

import io.github.rowak.nanoleafapi.NanoleafCallback;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;

public class CLICommand {
	
	public static final int ON = 0;
	public static final int OFF = 1;
	public static final int TOGGLE = 2;
	public static final int SET_BRIGHTNESS = 3;
	public static final int INCR_BRIGHTNESS = 4;
	public static final int SET_TEMP = 5;
	public static final int INCR_TEMP = 6;
	public static final int EFFECT = 7;
	public static final int RGB = 8;
	public static final int HSB = 9;
	
	public static final String[] COMMANDS = {"on", "off", "toggle", "brightness",
			null, "temp", null, "effect", "rgb", "hsb"};
	
	private int action;
	private String arg;
	
	public CLICommand(int action, String arg) {
		this.action = action;
		this.arg = arg;
	}
	
	public static int getCommandId(String commandStr, String arg) {
		for (int i = 0; i < COMMANDS.length; i++) {
			if (COMMANDS[i] != null && COMMANDS[i].equals(commandStr)) {
				if (i == SET_BRIGHTNESS || i == SET_TEMP) {
					// Note: decreasing brightness is the same as increasing by negative brightness
					if (arg.startsWith("+") || arg.startsWith("-")) {
						i++;
					}
				}
				return i;
			}
		}
		return -1;
	}
	
	public boolean isValid() {
		return action != -1;
	}
	
	public int getAction() {
		return action;
	}
	
	public String getArg() {
		return arg;
	}
	
	public int getArgAsInt() {
		return Integer.parseInt(arg);
	}
	
	public void execute(NanoleafGroup group)
			throws NanoleafException, IOException {
		switch (action) {
			case ON: group.setOnAsync(true, (status, data, device) -> checkStatus(status, device)); break;
			case OFF: group.setOnAsync(false, (status, data, device) -> checkStatus(status, device)); break;
			case TOGGLE: group.toggleOnAsync((status, data, device) -> checkStatus(status, device)); break;
			case SET_BRIGHTNESS: group.setBrightnessAsync(getArgAsInt(), (status, data, device) -> checkStatus(status, device)); break;
			case INCR_BRIGHTNESS: group.increaseBrightnessAsync(getArgAsInt(), (status, data, device) -> checkStatus(status, device)); break;
			case SET_TEMP: group.setColorTemperatureAsync(getArgAsInt(), (status, data, device) -> checkStatus(status, device)); break;
			case INCR_TEMP: group.increaseColorTemperatureAsync(getArgAsInt(), (status, data, device) -> checkStatus(status, device)); break;
			case EFFECT: group.setEffectAsync(arg, (status, data, device) -> checkStatus(status, device)); break;
			case RGB: break;
			case HSB: break;
		}
	}
	
	private void checkStatus(int status, NanoleafDevice device) {
		if (status != NanoleafCallback.SUCCESS) {
			System.out.println(String.format("Failed to execute action on %s. Error %d.",
					device.getName(), status));
		}
	}
	
	/*
	 * nleaf [-g name] [-d name] command
	 * 
	 * nleaf -g name create-group ip1,ip2
	 * nleaf -g name delete-group
	 * 
	 * Commands:
	 * 	- on
	 *  - off
	 *  - toggle
	 *  - brightness [+x/-x/x]
	 *  - temp [+x/-x/x]
	 *  - effect [name]
	 *  - rgb [rrr,ggg,bbb]
	 *  - rgb [#rrggbb]
	 *  - hsb [hhh,sss,bbb]
	 */
}
