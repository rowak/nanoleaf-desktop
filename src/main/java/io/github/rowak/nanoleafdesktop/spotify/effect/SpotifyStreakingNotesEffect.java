package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StaticEffect;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class SpotifyStreakingNotesEffect extends SpotifyEffect {
	
	private Map<NanoleafDevice, List<Panel>> edges;
	private List<Float> times;
	private Random random;
	private PanelCanvas canvas;
	
	public SpotifyStreakingNotesEffect(Color[] palette, NanoleafGroup group, PanelCanvas canvas) {
		super(SpotifyEffectType.STREAKING_NOTES, palette, group);
		requiresExtControl = true;
		this.canvas = canvas;
		init();
	}

	@Override
	public void init() {
		random = new Random();
		times = new ArrayList<Float>();
	}
	
	@Override
	public void reset() {
		times.clear();
	}

	@Override
	public void run(SpecificAudioAnalysis analysis)
			throws NanoleafException, IOException {
		AudioAnalysisSegment segment = analysis.getSegment();
		
		if (segment != null && palette.length > 0 &&
				!times.contains(segment.getMeasure().getStart())) {
			times.add(segment.getMeasure().getStart());
			group.forEach((device) -> {
				new Thread(() -> {
					List<Panel> path = getPath(device);
					StaticEffect.Builder seb = new StaticEffect.Builder(panels.get(device));
					for (int i = 0; i < path.size(); i++) {
						try {
							Color dark = Color.fromRGB((int)(palette[paletteIndex].getRed()*0.5),
									(int)(palette[paletteIndex].getGreen()*0.5),
									(int)(palette[paletteIndex].getBlue()*0.5));
							seb.setPanel(path.get(i), new Frame(dark, 3));
							if (i+1 < path.size()) {
								seb.setPanel(path.get(i+1), new Frame(palette[paletteIndex], 3));
							}
							Thread.sleep((int)(path.size()/
									segment.getMeasure().getDuration())*6);
							seb.setPanel(path.get(i), new Frame(Color.BLACK, 3));
							device.sendStaticEffectExternalStreaming(seb.build(null));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			});
			setNextPaletteColor();
		}
	}
	
	private List<Panel> getPath(NanoleafDevice device) {
		List<Panel> path = new ArrayList<Panel>();
		List<Panel> localPanels = panels.get(device);
		List<Panel> localEdges = edges.get(device);
		Panel start = localEdges.get(random.nextInt(edges.size()));
		path.add(start);
		List<Panel> neighbors = device.getNeighborPanels(start, localPanels);
		Panel p = neighbors.get(random.nextInt(neighbors.size()));
		path.add(p);
		while (!isEdgePanel(device, p, localPanels)) {
			neighbors = device.getNeighborPanels(p, localPanels);
			int i = random.nextInt(neighbors.size());
			if (!path.contains(neighbors.get(i))) {
				p = neighbors.get(i);
				path.add(p);
			}
		}
		return path;
	}
	
	private boolean isEdgePanel(NanoleafDevice device, Panel p, List<Panel> panels)
	{
		return device.getNeighborPanels(p, panels).size() < 2;
	}
	
	// An "edge" panel is defined as a panel with less than two neighbors
	private void getEdgePanels() {
		edges = new HashMap<NanoleafDevice, List<Panel>>();
		group.forEach((device) -> {
			List<Panel> localPanels = panels.get(device); 
			List<Panel> localEdges = new ArrayList<Panel>();
			for (Panel p : localPanels) {
				if (isEdgePanel(device, p, localPanels)) {
					localEdges.add(p);
				}
			}
			System.out.println(localEdges.size());
			edges.put(device, localEdges);
		});
	}
}
