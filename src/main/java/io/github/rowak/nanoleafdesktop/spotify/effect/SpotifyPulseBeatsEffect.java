package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafapi.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;

public class SpotifyPulseBeatsEffect extends SpotifyEffect
{
	private float loudness = 0.5f;
	private Random random;
	
	public SpotifyPulseBeatsEffect(Color[] palette, Aurora[] auroras)
	{
		super(SpotifyEffectType.PULSE_BEATS, palette, auroras);
		random = new Random();
	}
	
	@Override
	public void init(){}
	
	@Override
	public void reset(){}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws StatusCodeException, IOException
	{
		loudness = SpotifyEffectUtils.getLoudness(loudness, analysis);
		
		if (analysis.getBeat() != null && palette.length > 0)
		{
			for (int i = 0; i < auroras.length; i++)
			{
				int panelIndex = random.nextInt(panels[i].length);
				int panelId = panels[i][panelIndex].getId();
				int r = palette[paletteIndex].getRed();
				int g = palette[paletteIndex].getGreen();
				int b = palette[paletteIndex].getBlue();
				java.awt.Color original = new java.awt.Color(r, g, b);
				original = applyLoudnessToColor(original);
				java.awt.Color darker = original.darker().darker().darker();
				CustomEffectBuilder ceb = new CustomEffectBuilder(auroras[i]);
				ceb.addFrame(panelId, new Frame(original.getRed(),
						original.getGreen(), original.getBlue(), 0, 1));
				ceb.addFrame(panelId, new Frame(0, 0, 0, 0, 5));
				List<Integer> marked = new ArrayList<Integer>();
				marked.add(panelId);
				final int INITIAL_TIME = 1;
				setNeighbors(i, panels[i][panelIndex], marked, 
						panels, ceb, darker, INITIAL_TIME);
				
				final int fi = i;
				new Thread(() ->
				{
					try
					{
						auroras[fi].effects().displayEffect(ceb.build("", false));
					}
					catch (StatusCodeException sce)
					{
						sce.printStackTrace();
					}
				}).start();
			}
			setNextPaletteColor();
		}
	}
	
	public void setNeighbors(int auroraIndex, Panel panel, final List<Integer> marked,
			Panel[][] panels, CustomEffectBuilder ceb, java.awt.Color color,
			int time) throws StatusCodeException, IOException
	{
		time += 1;
		for (Panel p : panel.getNeighbors(panels[auroraIndex]))
		{
			if (!marked.contains(p.getId()))
			{
				ceb.addFrame(p, new Frame(color.getRed(),
						color.getGreen(), color.getBlue(), 0, time));
				ceb.addFrame(p, new Frame(0, 0, 0, 0, 5));
				marked.add(p.getId());
				setNeighbors(auroraIndex, p, marked, panels, ceb, color, time);
			}
		}
	}
	
	private java.awt.Color applyLoudnessToColor(java.awt.Color color)
	{
		float[] hsb = new float[3];
		hsb = java.awt.Color.RGBtoHSB(color.getRed(),
				color.getGreen(), color.getBlue(), hsb);
		hsb[2] = ((hsb[2]*100f)*loudness)/100f;
		color = java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		return color;
	}
}
