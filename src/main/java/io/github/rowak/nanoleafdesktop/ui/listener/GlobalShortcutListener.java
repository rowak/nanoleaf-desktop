package io.github.rowak.nanoleafdesktop.ui.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafdesktop.shortcuts.RunType;
import io.github.rowak.nanoleafdesktop.shortcuts.Shortcut;

public class GlobalShortcutListener implements NativeKeyListener {
	
	private List<String> pressedKeys;
	private List<String> processes;
	private List<Shortcut> shortcuts;
	private NanoleafGroup group;
	private Effect[] effects;
	
	public GlobalShortcutListener(List<Shortcut> shortcuts, NanoleafGroup group) {
		this.shortcuts = shortcuts;
		this.group = group;
		pressedKeys = new ArrayList<String>();
		processes = new ArrayList<String>();
		getEffects();
	}
	
	public void setAuroras(NanoleafGroup group) {
		this.group = group;
		getEffects();
	}
	
	private void checkKeyShortcuts(int trigger) {
		for (Shortcut s : shortcuts) {
			if (s.getKeys().equals(pressedKeys) &&
					s.getRunType() != RunType.WHILE_HELD && trigger == 0) {
				s.execute(group, effects);
			}
			else if (s.getKeys().equals(pressedKeys) &&
					s.getRunType() == RunType.WHILE_HELD &&
					s.getAction().getStates().size() == 0) {
				s.execute(group, effects);
			}
			else if (!s.getKeys().equals(pressedKeys) &&
					s.getRunType() == RunType.WHILE_HELD &&
					s.getAction().getStates().size() > 0) {
				s.getAction().reset(group, effects);
			}
		}
	}
	
	public void checkAppShortcuts() {
		try {
			refreshPidData();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		for (Shortcut s : shortcuts) {
			RunType runType = s.getRunType();
			if (runType == RunType.WHEN_APP_RUN ||
					runType == RunType.WHEN_APP_CLOSED) {
				Object[] args = s.getAction().getArgs();
				String appName = (String)args[1];
				boolean running = false;
				try {
					running = (boolean)args[2];
				}
				catch (ArrayIndexOutOfBoundsException obe) {
					s.getAction().setArgs(new Object[]{args[0], appName, running});
				}
				if ((processes.contains(appName) ||
						processes.contains(appName.replace(".exe", "")))) {
					if (!running && runType == RunType.WHEN_APP_RUN) {
						s.execute(group, effects);
					}
					s.getAction().setArgs(new Object[]{args[0], appName, true});
				}
				else {
					if (running && runType == RunType.WHEN_APP_CLOSED) {
						s.execute(group, effects);
					}
					s.getAction().setArgs(new Object[]{args[0], appName, false});
				}
			}
		}
	}
	
	private void refreshPidData() throws IOException {
		processes.clear();
		Process process = Runtime.getRuntime().exec("tasklist.exe");
		BufferedReader input = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		String line;
		int lineNum = 0;
		while ((line = input.readLine()) != null) {
		    if (lineNum > 2) {
		    	line = line.substring(0, 26).trim();
		    	processes.add(line);
		    }
		    lineNum++;
		}
		input.close();
	}
	
	private void getEffects() {
		new Thread(() -> {
			List<Effect> effectsList = new ArrayList<Effect>();
			try {
				effectsList = group.getAllEffects();
				effects = new Effect[effectsList.size()];
				for (int i = 0; i < effects.length; i++) {
					effects[i] = effectsList.get(i);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		String key = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (!pressedKeys.contains(key)) {
			pressedKeys.add(key);
		}
		checkKeyShortcuts(0);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		String key = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (pressedKeys.contains(key)) {
			pressedKeys.remove(key);
		}
		checkKeyShortcuts(1);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {}
}
