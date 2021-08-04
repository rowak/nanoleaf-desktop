package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;

public class SpotifySwipeBeatsEffect extends SpotifyEffect {
	
	private final int NUM_DIRECTIONS = 8;
	private final int ANGLE_INCREMENT = 45; // degrees
	
	private Random random;
	
	/*
	 * Each Nanoleaf device maps to a list of panel tables.
	 * Each panel table corresponds to one of eight directions.
	 * All eight panel tables are calculated initially to improve
	 * performance when the effect is running.
	 */
	private Map<NanoleafDevice, List<Panel[][]>> panelTables;
	
	public SpotifySwipeBeatsEffect(Color[] palette, NanoleafGroup group) {
		super(SpotifyEffectType.SWIPE_BEATS, palette, group);
		requiresExtControl = true;
		init();
	}
	
	public enum SwipeDirection {
		SOUTH,     // 0
		SOUTHWEST, // pi/4
		WEST,      // pi/2
		NORTHWEST, // 3*pi/4
		NORTH,     // pi
		NORTHEAST, // 5*pi/4
		EAST,      // 3*pi/2
		SOUTHEAST  // 7*pi/4
	}

	@Override
	public void init() {
		initPanelTables();
		random = new Random(System.nanoTime());
	}
	
	@Override
	public void reset() {
		
	}

	@Override
	public void run(SpecificAudioAnalysis analysis)
			throws NanoleafException, IOException {
		if (analysis.getBeat() != null && palette.length > 0) {
			group.forEach((device) -> {
				new Thread(() -> {
					int rotationIndex = random.nextInt(NUM_DIRECTIONS);
					try {
						CustomEffect path = getPath(device, rotationIndex);
						if (path != null) {
							device.displayEffect(path);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			});
			setNextPaletteColor();
		}
	}
	
	private void initPanelTables() {
		panelTables = new HashMap<>();
		group.forEach((device) -> {
			try {
				List<Panel[][]> directionTables = new ArrayList<>();
				for (int i = 0; i < NUM_DIRECTIONS; i++) {
					List<Panel> rotatedPanels = getPanelsRotated(device, i*ANGLE_INCREMENT);
					Panel[][] table = PanelTableSort.getRows(rotatedPanels.toArray(new Panel[0]));
					directionTables.add(table);
				}
				panelTables.put(device, directionTables);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	/*
	 * Creates a single "swipe" effect for a device in one of the eight directions.
	 */
	private CustomEffect getPath(NanoleafDevice device, int rotationIndex) {
		CustomEffect.Builder builder = new CustomEffect.Builder(panels.get(device));
		List<Panel[][]> directionTables = panelTables.get(device);
		Panel[][] table = directionTables.get(rotationIndex);
		
		int h = palette[paletteIndex].getHue();
		int s = palette[paletteIndex].getSaturation();
		int b = palette[paletteIndex].getBrightness();
		Color color = Color.fromHSB(h, s, b);
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
//				builder.addFrame(table[i][j], new Frame(Color.BLACK, i));
				builder.addFrame(table[i][j], new Frame(color, i));
				builder.addFrame(table[i][j], new Frame(Color.BLACK, 2));
			}
			color.setHue((color.getHue()-10)%360);
		}
		
		try {
			return builder.build("", false);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/* Modified API methods for panel rotation */
	
	private List<Panel> getPanelsRotated(NanoleafDevice device, int rotation)
			throws NanoleafException, IOException {
		List<Panel> localPanels = panels.get(device);
		Point origin = getLayoutCentroid(localPanels);
		rotation = rotation == 360 ? 0 : rotation;
		double radAngle = Math.toRadians(rotation);
		for (Panel p : localPanels) {
			int x = p.getX() - origin.x;
			int y = p.getY() - origin.y;
			
			double newX = x * Math.cos(radAngle) - y * Math.sin(radAngle);
			double newY = x * Math.sin(radAngle) + y * Math.cos(radAngle);
			
			x = (int)(newX + origin.x);
			y = (int)(newY + origin.y);
			p.setX(x);
			p.setY(y);
		}
		return localPanels;
	}
	
	private Point getLayoutCentroid(List<Panel> panels) {
		int centroidX = 0, centroidY = 0;
		int numXPoints = 0, numYPoints = 0;
		List<Integer> xpoints = new ArrayList<Integer>();
		List<Integer> ypoints = new ArrayList<Integer>();
		
		for (Panel p : panels) {
			int x = p.getX();
			int y = p.getY();
			if (!xpoints.contains(x)) {
				centroidX += x;
				xpoints.add(x);
				numXPoints++;
			}
			if (!ypoints.contains(y)) {
				centroidY += y;
				ypoints.add(y);
				numYPoints++;
			}
		}
		centroidX /= numXPoints;
		centroidY /= numYPoints;
		return new Point(centroidX, centroidY);
	}
}
