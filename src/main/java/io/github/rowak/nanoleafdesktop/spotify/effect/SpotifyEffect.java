package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;

public abstract class SpotifyEffect
{
	protected boolean requiresExtControl;
	protected int paletteIndex;
	protected SpotifyEffectType type;
	protected Aurora[] auroras;
	protected Panel[][] panels;
	protected Color[] palette;
	protected List<UserOption> userOptions;
	
	public SpotifyEffect(SpotifyEffectType type,
			Color[] palette, Aurora[] auroras)
	{
		this.type = type;
		this.palette = palette;
		this.auroras = auroras;
		try
		{
			panels = new Panel[auroras.length][];
			for (int i = 0; i < auroras.length; i++)
			{
				panels[i] = auroras[i].panelLayout().getPanelsRotated();
			}
		}
		catch (StatusCodeException sce)
		{
			panels = new Panel[auroras.length][];
		}
		userOptions = new ArrayList<UserOption>();
	}
	
	// Called when the effect is first created or needs to be hard-reset
	public abstract void init()
			throws StatusCodeException, IOException;
	
	// Called when the progress of the local player increases
	public abstract void run(SpecificAudioAnalysis analysis)
			throws StatusCodeException, IOException;
	
	// Called when the status of the track is modified (play/pause/seek)
	public abstract void reset();
	
	public SpotifyEffectType getType()
	{
		return type;
	}
	
	public boolean requiresExtControl()
	{
		return requiresExtControl;
	}
	
	public List<UserOption> getUserOptions()
	{
		return userOptions;
	}
	
	public void setPalette(Color[] palette)
			throws IOException, StatusCodeException
	{
		this.palette = palette;
		paletteIndex = palette.length > 1 ? 1 : 0;
		init();
	}
	
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
