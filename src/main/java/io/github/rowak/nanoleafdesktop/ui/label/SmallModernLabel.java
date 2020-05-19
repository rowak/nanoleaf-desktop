package io.github.rowak.nanoleafdesktop.ui.label;

import javax.swing.JLabel;

import io.github.rowak.nanoleafdesktop.tools.UIConstants;

public class SmallModernLabel extends JLabel
{
	public SmallModernLabel()
	{
		init();
	}
	
	public SmallModernLabel(String text)
	{
		init();
		setText(text);
	}
	
	private void init()
	{
		setFont(UIConstants.smallLabelFont);
		setForeground(UIConstants.textPrimary);
	}
}
