package io.github.rowak.nanoleafdesktop.ui.menu;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JMenuItem;

public class ModernMenuItem extends JMenuItem
{
	public ModernMenuItem()
	{
		init();
	}
	
	public ModernMenuItem(String text)
	{
		init();
		setText(text);
	}
	
	private void init()
	{
		setBackground(Color.DARK_GRAY);
		setForeground(Color.WHITE);
		setFont(new Font("Tahoma", Font.PLAIN, 20));
	}
}
