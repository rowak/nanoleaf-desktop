package io.github.rowak.nanoleafdesktop.ui.panel.spotify;

import java.io.IOException;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;

public abstract class SpotifyEffect
{
	protected int paletteIndex;
	protected Type type;
	protected Aurora aurora;
	protected Panel[] panels;
	protected Color[] palette;
	
	public static enum Type
	{
		PULSE_BEATS
	}
	
	public SpotifyEffect(Type type,
			Color[] palette, Aurora aurora)
	{
		this.type = type;
		this.palette = palette;
		this.aurora = aurora;
		try
		{
			panels = aurora.panelLayout().getPanels();
		}
		catch (StatusCodeException sce)
		{
			panels = new Panel[0];
		}
	}
	
	public void setPalette(Color[] palette)
	{
		this.palette = palette;
		paletteIndex = 0;
	}
	
	public abstract void runBeat()
			throws StatusCodeException, IOException;
	
	protected void setNextPaletteColor()
	{
		if (paletteIndex == palette.length-1)
		{
			paletteIndex = 0;
		}
		else
		{
			paletteIndex++;
		}
	}
}
