package io.github.rowak.nanoleafdesktop.ui.menu;

import java.awt.Color;

import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

public class ModernPopupMenu extends JPopupMenu
{
	public ModernPopupMenu()
	{
		init();
	}
	
	private void init()
	{
		setBackground(Color.DARK_GRAY);
		setBorder(new LineBorder(Color.WHITE, 1, true));
	}
}
