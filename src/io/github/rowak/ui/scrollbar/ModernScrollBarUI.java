package io.github.rowak.ui.scrollbar;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ModernScrollBarUI extends BasicScrollBarUI
{
	@Override
	protected JButton createIncreaseButton(int orientation)
	{
		thumbColor = Color.GRAY;
		thumbLightShadowColor = new Color(0, 0, 0, 0);
		thumbDarkShadowColor = new Color(0, 0, 0, 0);
		thumbHighlightColor = Color.GRAY;
		trackColor = new Color(57, 57, 57);
		trackHighlightColor = Color.GRAY;
		return new BasicArrowButton(BasicArrowButton.SOUTH,
				Color.GRAY, Color.GRAY, new Color(57, 57, 57), Color.LIGHT_GRAY);
	}
	
	@Override
	protected JButton createDecreaseButton(int orientation)
	{
		thumbColor = Color.GRAY;
		thumbLightShadowColor = new Color(0, 0, 0, 0);
		thumbDarkShadowColor = new Color(0, 0, 0, 0);
		thumbHighlightColor = Color.GRAY;
		trackColor = new Color(57, 57, 57);
		trackHighlightColor = Color.GRAY;
		return new BasicArrowButton(BasicArrowButton.SOUTH,
				Color.GRAY, Color.GRAY, new Color(57, 57, 57), Color.LIGHT_GRAY);
	}
}
