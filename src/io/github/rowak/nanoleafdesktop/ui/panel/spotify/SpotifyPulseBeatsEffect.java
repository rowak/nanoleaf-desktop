package io.github.rowak.nanoleafdesktop.ui.panel.spotify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.effectbuilder.CustomEffectBuilder;

public class SpotifyPulseBeatsEffect extends SpotifyEffect
{
	private Random random;
	
	public SpotifyPulseBeatsEffect(Color[] palette, Aurora aurora)
	{
		super(SpotifyEffect.Type.PULSE_BEATS, palette, aurora);
		random = new Random();
	}
	
	@Override
	public void runBeat()
					throws StatusCodeException, IOException
	{
		int panelIndex = random.nextInt(panels.length);
		int panelId = panels[panelIndex].getId();
		int r = palette[paletteIndex].getRed();
		int g = palette[paletteIndex].getGreen();
		int b = palette[paletteIndex].getBlue();
		java.awt.Color original = new java.awt.Color(r, g, b);
		java.awt.Color darker = original.darker().darker().darker();
		CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
		ceb.addFrame(panelId, new Frame(r, g, b, 0, 1));
		ceb.addFrame(panelId, new Frame(0, 0, 0, 0, 5));
		List<Integer> marked = new ArrayList<Integer>();
		marked.add(panelId);
		final int INITIAL_TIME = 1;
		setNeighbors(panels[panelIndex], marked, 
				panels, ceb, darker, INITIAL_TIME);
		aurora.effects().displayEffect(ceb.build("", false));
		setNextPaletteColor();
	}
	
	public void setNeighbors(Panel panel, final List<Integer> marked,
			Panel[] panels, CustomEffectBuilder ceb, java.awt.Color color,
			int time) throws StatusCodeException, IOException
	{
		time += 1;
		for (Panel p : panel.getNeighbors(panels))
		{
			if (!marked.contains(p.getId()))
			{
				ceb.addFrame(p, new Frame(color.getRed(),
						color.getGreen(), color.getBlue(), 0, time));
				ceb.addFrame(p, new Frame(0, 0, 0, 0, 5));
				marked.add(p.getId());
				setNeighbors(p, marked, panels, ceb, color, time);
			}
		}
	}
}
