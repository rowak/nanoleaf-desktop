package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;

public class SpotifyFireworksEffect extends SpotifyEffect
{
	private float loudness = 0.5f;
	private Random random;
	
	public SpotifyFireworksEffect(Color[] palette, Aurora aurora)
	{
		super(SpotifyEffectType.FIREWORKS, palette, aurora);
		requiresExtControl = true;
		random = new Random();
	}

	@Override
	public void init(){}

	@Override
	public void run(SpecificAudioAnalysis analysis)
			throws StatusCodeException, IOException
	{
		loudness = SpotifyEffectUtils.getLoudness(loudness, analysis);
		
		AudioAnalysisMeasure beat = analysis.getBeat();
		
		if (beat != null && palette.length > 0)
		{
			new Thread(() ->
			{				
				try
				{
					int colorIndex = random.nextInt(palette.length-1);
					List<Panel> updatedPanels = new ArrayList<Panel>();
					for (Panel p : panels)
					{
						if (random.nextBoolean())
						{
							updatedPanels.add(p);
							int r = palette[colorIndex].getRed();
							int g = palette[colorIndex].getGreen();
							int b = palette[colorIndex].getBlue();
							java.awt.Color color = applyLoudnessToColor(new java.awt.Color(r, g, b));
							aurora.externalStreaming().setPanel(p, color.getRed(),
									color.getGreen(), color.getBlue(), 1);
						}
					}
					
					Thread.sleep(50);
					
					for (Panel p : updatedPanels)
					{
						aurora.externalStreaming().setPanel(p, 0, 0, 0,
								(int)(beat.getDuration()*10));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}).start();
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
