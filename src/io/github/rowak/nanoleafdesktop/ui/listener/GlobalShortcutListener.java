package io.github.rowak.nanoleafdesktop.ui.listener;

import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.github.rowak.Aurora;
import io.github.rowak.nanoleafdesktop.shortcuts.RunType;
import io.github.rowak.nanoleafdesktop.shortcuts.Shortcut;

public class GlobalShortcutListener implements NativeKeyListener
{
	private List<String> pressedKeys;
	private List<Shortcut> shortcuts;
	private Aurora device;
	
	public GlobalShortcutListener(List<Shortcut> shortcuts, Aurora device)
	{
		this.shortcuts = shortcuts;
		this.device = device;
		pressedKeys = new ArrayList<String>();
	}
	
	public void setAurora(Aurora device)
	{
		this.device = device;
	}
	
	private void checkShortcut(int trigger)
	{
		for (Shortcut s : shortcuts)
		{
			if (s.getKeys().equals(pressedKeys) &&
					s.getRunType() != RunType.WHILE_HELD && trigger == 0)
			{
				s.execute(device);
			}
			else if (s.getKeys().equals(pressedKeys) &&
					s.getRunType() == RunType.WHILE_HELD &&
					s.getAction().getPreviousState() == null)
			{
				s.execute(device);
			}
			else if (!s.getKeys().equals(pressedKeys) &&
					s.getRunType() == RunType.WHILE_HELD &&
					s.getAction().getPreviousState() != null)
			{
				s.getAction().reset(device);
			}
		}
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent e)
	{
		String key = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (!pressedKeys.contains(key))
		{
			pressedKeys.add(key);
		}
		checkShortcut(0);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e)
	{
		String key = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (pressedKeys.contains(key))
		{
			pressedKeys.remove(key);
		}
		checkShortcut(1);
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {}
}
