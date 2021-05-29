package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.CustomEffect;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;

public class SpotifyPulseBeatsEffect extends SpotifyEffect {
	private float loudness = 0.5f;
	private Random random;
	
	public SpotifyPulseBeatsEffect(Color[] palette, NanoleafGroup group) {
		super(SpotifyEffectType.PULSE_BEATS, palette, group);
		random = new Random();
	}
	
	@Override
	public void init(){}
	
	@Override
	public void reset(){}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws NanoleafException, IOException {
		loudness = SpotifyEffectUtils.getLoudness(loudness, analysis);
		
		if (analysis.getBeat() != null && palette.length > 0) {
			group.forEach((device) -> {
				int panelIndex = random.nextInt(panels.get(device).size());
				int panelId = panels.get(device).get(panelIndex).getId();
				int r = palette[paletteIndex].getRed();
				int g = palette[paletteIndex].getGreen();
				int b = palette[paletteIndex].getBlue();
				java.awt.Color original = new java.awt.Color(r, g, b);
				original = applyLoudnessToColor(original);
				java.awt.Color darker = original.darker().darker().darker();
				CustomEffect.Builder ceb = new CustomEffect.Builder(panels.get(device));
				ceb.addFrame(panelId, new Frame(original.getRed(),
						original.getGreen(), original.getBlue(), 1));
				ceb.addFrame(panelId, new Frame(0, 0, 0, 5));
				int pi2 = random.nextInt(panels.get(device).size());
				int pid2 = panels.get(device).get(pi2).getId();
				ceb.addFrame(pid2, new Frame(original.getRed(),
						original.getGreen(), original.getBlue(), 1));
				ceb.addFrame(pid2, new Frame(0, 0, 0, 15));
				List<Integer> marked = new ArrayList<Integer>();
				marked.add(panelId);
				marked.add(pid2);
				final int INITIAL_TIME = 1;
				
				try {
					setNeighbors(device, panels.get(device).get(panelIndex), marked, 
							ceb, darker, INITIAL_TIME);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				new Thread(() -> {
					try {
						device.displayEffect(ceb.build(null, false));
					}
					catch (NanoleafException | IOException e) {
						e.printStackTrace();
					}
				}).start();
			});
			setNextPaletteColor();
		}
	}
	
	public void setNeighbors(NanoleafDevice device, Panel panel, final List<Integer> marked,
			CustomEffect.Builder ceb, java.awt.Color color,
			int time) throws NanoleafException, IOException {
		time += 1;
		for (Panel p : getNeighbors(panel, panels.get(device))) {
			if (!marked.contains(p.getId())) {
				ceb.addFrame(p, new Frame(color.getRed(),
						color.getGreen(), color.getBlue(), time));
				ceb.addFrame(p, new Frame(0, 0, 0, 5));
				marked.add(p.getId());
				setNeighbors(device, p, marked, ceb, color, time);
			}
		}
	}
	
	public Panel[] getNeighbors(Panel panel, List<Panel> panels) {
		// Distance constant represents (about) the vertical/horizontal/diagonal distance
		// that all neighboring panels are within
		final int DISTANCE_CONST = 116;
		List<Panel> neighbors = new ArrayList<Panel>();
		int p1x = panel.getX();
		int p1y = panel.getY();
		for (Panel p2 : panels) {
			int p2x = p2.getX();
			int p2y = p2.getY();
			if (Math.floor(Math.sqrt(Math.pow((p1x - p2x), 2) +
					Math.pow((p1y - p2y), 2))) <= DISTANCE_CONST) {
				neighbors.add(p2);
			}
		}
		return neighbors.toArray(new Panel[]{});
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
