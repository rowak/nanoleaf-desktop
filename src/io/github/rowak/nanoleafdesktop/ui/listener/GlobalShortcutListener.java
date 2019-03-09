package io.github.rowak.nanoleafdesktop.ui.listener;

import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import io.github.rowak.Aurora;
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
	
	private void checkShortcut()
	{
		for (Shortcut s : shortcuts)
		{
			if (s.getKeys().equals(pressedKeys))
			{
				s.execute(device);
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
		checkShortcut();
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e)
	{
		String key = NativeKeyEvent.getKeyText(e.getKeyCode());
		if (pressedKeys.contains(key))
		{
			pressedKeys.remove(key);
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {}
}
