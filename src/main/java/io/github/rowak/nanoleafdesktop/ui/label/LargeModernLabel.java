package io.github.rowak.nanoleafdesktop.ui.label;

import javax.swing.JLabel;

import io.github.rowak.nanoleafdesktop.tools.UIConstants;

public class LargeModernLabel extends JLabel
{
	public LargeModernLabel()
	{
		init();
	}
	
	public LargeModernLabel(String text)
	{
		init();
		setText(text);
	}
	
	private void init()
	{
		setFont(UIConstants.largeLabelFont);
		setForeground(UIConstants.textPrimary);
	}
}
