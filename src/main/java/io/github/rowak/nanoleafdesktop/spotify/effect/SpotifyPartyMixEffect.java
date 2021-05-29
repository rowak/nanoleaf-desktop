package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StaticEffect;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;

/*
 * This effect is nearly identical to the Fireworks Spotify effect.
 * The only changes are:
 *   - 70% of the time, all the panels will be activated. The other 30%
 *     of the time, only a few randomly selected panels will be activated
 *   - The fade out duration has been increased by 50%
 */
public class SpotifyPartyMixEffect extends SpotifyEffect {

	private float loudness = 0.5f;
	private Random random;
	
	public SpotifyPartyMixEffect(Color[] palette, NanoleafGroup group) {
		super(SpotifyEffectType.PARTY_MIX, palette, group);
		requiresExtControl = true;
		random = new Random();
	}

	@Override
	public void init(){}
	
	@Override
	public void reset(){}

	@Override
	public void run(SpecificAudioAnalysis analysis)
			throws NanoleafException, IOException
	{
		loudness = SpotifyEffectUtils.getLoudness(loudness, analysis);
		
		AudioAnalysisMeasure beat = analysis.getBeat();
		
		if (beat != null && palette.length > 0) {
			group.forEach((device) -> {
				new Thread(() -> {
					int mode = random.nextInt(10);
					int colorIndex = random.nextInt(palette.length);
					List<Panel> updatedPanels = new ArrayList<Panel>();
					StaticEffect.Builder ef = new StaticEffect.Builder(panels.get(device));
					for (Panel p : panels.get(device)) {
						if (p.getId() == 0) {
							continue;
						}
						if (mode < 7 || random.nextBoolean()) {
							updatedPanels.add(p);
							int r = palette[colorIndex].getRed();
							int g = palette[colorIndex].getGreen();
							int b = palette[colorIndex].getBlue();
							java.awt.Color color =
									applyLoudnessToColor(new java.awt.Color(r, g, b));
							ef.setPanel(p, new Frame(color.getRed(),
										color.getGreen(), color.getBlue(), 0));
						}
					}
					
					try {
						device.sendStaticEffectExternalStreaming(ef.build(null));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						Thread.sleep(200);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					ef = new StaticEffect.Builder(panels.get(device));
					for (Panel p : updatedPanels) {
						try {
							ef.setPanel(p, new Frame(0, 0, 0,
									(int)(beat.getDuration()*20)));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					try {
						device.sendStaticEffectExternalStreaming(ef.build(null));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			});
		}
	}
	
	private java.awt.Color applyLoudnessToColor(java.awt.Color color) {
		float[] hsb = new float[3];
		hsb = java.awt.Color.RGBtoHSB(color.getRed(),
				color.getGreen(), color.getBlue(), hsb);
		hsb[2] = ((hsb[2]*100f)*loudness)/100f;
		color = java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		return color;
	}
}
