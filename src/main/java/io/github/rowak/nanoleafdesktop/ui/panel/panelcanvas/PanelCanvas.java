package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Canvas;
import io.github.rowak.nanoleafapi.CustomEffect;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.Shapes;
import io.github.rowak.nanoleafapi.StaticEffect;
import io.github.rowak.nanoleafapi.util.AnimationParser;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Palette;

public class PanelCanvas extends JPanel {
	
	private boolean initialized;
	private float scaleFactor = 1f;
	private NanoleafGroup group;
	private Map<NanoleafDevice, List<Panel>> panels;
	private Map<NanoleafDevice, Point> panelOffset;
	private Map<NanoleafDevice, Integer> rotation;
	private Map<NanoleafDevice, Integer> tempRotation;
	private Map<NanoleafDevice, Float> brightness;
	private Map<Integer, Color> panelColors;
	private PanelActionListener pdl;
	private CustomEffectDisplay customEffectDisplay;
	private LoadingSpinner spinner;
	private Graphics buffG;
	private BufferedImage buff;
	
	public PanelCanvas(NanoleafGroup group) {
		this.group = group;
		if (group != null && group.getDevices().size() == 1) {
			initCanvas();
		}
		startLoadingSpinner();
	}
	
	enum DeviceType {
		AURORA, CANVAS, HEXAGONS
	}
	
	public void initCanvas() {
		if (!initialized && this.isValid() &&
				group != null && panels == null) {
			try {
				panels = new HashMap<NanoleafDevice, List<Panel>>();
				panelOffset = new HashMap<NanoleafDevice, Point>();
				rotation = new HashMap<NanoleafDevice, Integer>();
				tempRotation = new HashMap<NanoleafDevice, Integer>();
				brightness = new HashMap<NanoleafDevice, Float>();
				panelColors = new HashMap<Integer, Color>();
				group.forEach((device) -> {
					try {
						panels.put(device, device.getPanelsRotated());
						panelOffset.put(device, new Point());
						rotation.put(device, 360 - device.getGlobalOrientation());
						tempRotation.put(device, rotation.get(device));
						boolean on = device.getOn();
						for (Panel p : panels.get(device)) {
							panelColors.put(p.getId(), on ? Color.WHITE : Color.BLACK);
						}
						brightness.put(device, device.getBrightness()/100f);
					}
					catch (NanoleafException e) {
						new TextDialog(this, "An error occurred while getting data from the device. " +
								"Please relaunch the application.").setVisible(true);
					}
					catch (IOException e) {
						new TextDialog(this, "Could not connect to the device. " +
								"Please relaunch the application.").setVisible(true);
					}
				});
			}
			catch (NullPointerException e) {
				panels = new HashMap<NanoleafDevice, List<Panel>>();
			}
			
			group.forEach((device) -> {
				List<Panel> localPanels = panels.get(device);
				for (Panel p : localPanels) {
					p.setX(p.getX() + getWidth()/2);
					p.setY(-p.getY() + getHeight()/2);
				}
				getDefaultPanelPositions(localPanels, device);
			});
			
			customEffectDisplay = new CustomEffectDisplay(this);
			toggleOn();
			
			if (pdl != null) {
				removeMouseListener(pdl);
				removeMouseMotionListener(pdl);
				removeMouseWheelListener(pdl);
			}
			
			pdl = new PanelActionListener(this, panels);
			addMouseListener(pdl);
			addMouseMotionListener(pdl);
			addMouseWheelListener(pdl);
			
			initialized = true;
		}
	}
	
	public void reinitialize() {
		initialized = false;
		panels = null;
		initCanvas();
	}
	
	private void startLoadingSpinner() {
		if (group != null && group.getGroupSize() > 0) {
			spinner = new LoadingSpinner(Color.DARK_GRAY);
			add(spinner);
		}
	}
	
	private void stopLoadingSpinner() {
		if (group.getGroupSize() > 0 && this.isAncestorOf(spinner)) {
			remove(spinner);
		}
	}
	
	public void setAuroras(NanoleafGroup group) {
		this.group = group;
		reinitialize();
	}
	
	public Map<NanoleafDevice, Point> getPanelOffset() {
		return panelOffset;
	}
	
	public void setPanelOffset(Map<NanoleafDevice, Point> panelOffset) {
		this.panelOffset = panelOffset;
	}
	
	public NanoleafGroup getAuroras() {
		return group;
	}
	
	public List<Panel> getPanels(NanoleafDevice device) {
		return panels.get(device);
	}
	
	public void setPanels(Map<NanoleafDevice, List<Panel>> panels) {
		this.panels = panels;
	}
	
	public List<Panel> getGroupPanels() {
		List<Panel> groupPanels = new ArrayList<Panel>();
		group.forEach((device) -> {
			Point localOffset = panelOffset.get(device);
			for (Panel p : panels.get(device)) {
				int x = (p.getX() + localOffset.x);
				int y = -(p.getY() + localOffset.y);
				groupPanels.add(new Panel(p.getId(),
						x, y, p.getOrientation(), device.getShapeType()));
			}
		});
		return groupPanels;
	}
	
	public int getRotation(NanoleafDevice device) {
		return rotation.get(device);
	}
	
	public void setRotation(int degrees, NanoleafDevice device) {
		try {
			degrees += rotation.get(device);
			if (degrees < 0) {
				degrees = 360 - Math.abs(degrees);
			}
			if (degrees > 360) {
				degrees = degrees % 360;
			}
			group.setGlobalOrientation(360-degrees);
			rotation.put(device, degrees);
			reloadPanels(device);
		}
		catch (NanoleafException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getTempRotation(NanoleafDevice device) {
		return tempRotation.get(device);
	}
	
	public void setTempRotation(int degrees, NanoleafDevice device) {
		tempRotation.put(device, degrees);
	}
	
	public float getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(float factor) {
		scaleFactor = factor;
	}
	
	public Map<NanoleafDevice, Float> getBrightness() {
		return brightness;
	}
	
	public void setBrightness(float brightness, NanoleafDevice device) {
		this.brightness.put(device, brightness);
		repaint();
	}
	
	public void setEffect(String effect, NanoleafDevice device) {
		try {
			String colorMode = device.getColorMode();
			if (colorMode.equals("hs") || colorMode.equals("ct")) {
				if (customEffectDisplay != null) {
					customEffectDisplay.stop();
				}
				
				int hue = device.getHue();
				int sat = device.getSaturation();
				int bri = device.getBrightness();
				setHSB(hue, sat, bri);
			}
			else if (colorMode.equals("effects") || colorMode.equals("effect")) {
				String currentEffectName = device.getCurrentEffectName();
				Effect currentEffect = device.getCurrentEffect();
				if (currentEffect != null && currentEffect.getEffectType() != null) {
					if (currentEffectName.equals("*Static*") ||
							currentEffect.getEffectType().equals("static")) {
						if (customEffectDisplay != null) {
							customEffectDisplay.stop();
						}
						setStaticEffect((StaticEffect)currentEffect, device);
					}
					else if (currentEffect.getEffectType().equals("custom")) {
						// **************** Currently disabled ****************
						//customEffectDisplay.changeEffect(currentEffect);
					}
					else if (!currentEffect.getEffectType().equals("static")) {
						if (customEffectDisplay != null) {
							customEffectDisplay.stop();
						}
						Palette palette = currentEffect.getPalette();
						int[] avgRgb = new int[3];
						if (palette != null) {
							int numColors = palette.getColors().size();
							for (io.github.rowak.nanoleafapi.Color c : palette.getColors()) {
								Color rgb = new Color(Color.HSBtoRGB(c.getHue()/360f,
										c.getSaturation()/100f, c.getBrightness()/100f));
								avgRgb[0] += rgb.getRed();
								avgRgb[1] += rgb.getGreen();
								avgRgb[2] += rgb.getBlue();
							}
							Color avgColor = new Color(avgRgb[0]/numColors,
									avgRgb[1]/numColors, avgRgb[2]/numColors);
							setColor(avgColor);
						}
					}
				}
			}
		}
		catch (NanoleafException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void reloadPanels(NanoleafDevice device) {
		try {
			panels.put(device, device.getPanelsRotated());
		}
		catch (NullPointerException e) {
			panels.put(device, new ArrayList<Panel>());
		}
		catch (IOException e) {
			new TextDialog(this, "Could not connect to the device. " +
					"Please relaunch the application.").setVisible(true);
		}
		catch (NanoleafException e) {
			new TextDialog(this, "An error occurred while getting data from the device. " +
					"Please relaunch the application.").setVisible(true);
		}
		
		List<Panel> localPanels = panels.get(device);
		for (Panel p : localPanels) {
			p.setX(p.getX() + getWidth()/2);
			p.setY(-p.getY() + getHeight()/2);
		}
		getDefaultPanelPositions(localPanels, device);
	}
	
	public void checkAuroraState(NanoleafDevice device) {
		if (device != null && group != null && group.getGroupSize() > 0) {
			stopLoadingSpinner();
			
			try {
				String colorMode = device.getColorMode();
				if (colorMode.equals("hs") || colorMode.equals("ct")) {
					if (customEffectDisplay != null) {
						customEffectDisplay.stop();
					}
					
					int hue = device.getHue();
					int sat = device.getSaturation();
					int bri = device.getBrightness();
					setHSB(hue, sat, bri);
				}
				else if (colorMode.equals("effects") || colorMode.equals("effect")) {
					String currentEffectName = device.getCurrentEffectName();
					Effect currentEffect = device.getCurrentEffect();
					if (currentEffect != null && currentEffect.getEffectType() != null) {
						if (currentEffectName.equals("*Static*") ||
								currentEffect.getEffectType().equals("static")) {
							if (customEffectDisplay != null) {
								customEffectDisplay.stop();
							}
							setStaticEffect((StaticEffect)currentEffect, device);
						}
						else if (currentEffect.getEffectType().equals("custom")) {
							// **************** Currently disabled ****************
							//customEffectDisplay.changeEffect(currentEffect);
						}
						else if (!currentEffect.getEffectType().equals("static")) {
							if (customEffectDisplay != null) {
								customEffectDisplay.stop();
							}
							Palette palette = currentEffect.getPalette();
							int[] avgRgb = new int[3];
							if (palette != null && palette.getColors().size() > 0) {
								int numColors = palette.getColors().size();
								for (io.github.rowak.nanoleafapi.Color c : palette.getColors()) {
									Color rgb = new Color(Color.HSBtoRGB(c.getHue()/360f,
											c.getSaturation()/100f, c.getBrightness()/100f));
									avgRgb[0] += rgb.getRed();
									avgRgb[1] += rgb.getGreen();
									avgRgb[2] += rgb.getBlue();
								}
								Color avgColor = new Color(avgRgb[0]/numColors,
										avgRgb[1]/numColors, avgRgb[2]/numColors);
								setColor(avgColor);
							}
						}
					}
				}
			}
			catch (NanoleafException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void checkAuroraStateForAll() {
		group.forEach((device) -> {
			checkAuroraState(device);
		});
	}
	
	public void toggleOn() {
		if (group.getGroupSize() > 0) {
			group.forEach((device) -> {
				try {
					Color c = device.getOn() ? Color.WHITE : Color.BLACK;
					setColor(c, device);
					
					if (!c.equals(Color.BLACK)) {
						checkAuroraState(device);
					}
					stopLoadingSpinner();
				}
				catch (NanoleafException | IOException e) {
					new TextDialog(this, "Lost connection to the device. " +
							"Please try again.").setVisible(true);
				}
			});
		}
	}
	
	public void setOn(boolean on, NanoleafDevice device) {
		if (on) {
			checkAuroraState(device);
		}
		else {
			setColor(Color.BLACK, device);
		}
		stopLoadingSpinner();
	}
	
	public Map<NanoleafDevice, Color> getColor() {
		Map<NanoleafDevice, Color> colors = new HashMap<NanoleafDevice, Color>();
		group.forEach((device) -> {
			boolean same = true;
			List<Panel> localPanels = panels.get(device);
			Color first = panelColors.get(localPanels.get(0).getId());
			for (Panel p : localPanels) {
				Color next = panelColors.get(p.getId());
				if (!first.equals(next)) {
					same = false;
					break;
				}
			}
			if (same) {
				colors.put(device, first);
			}
			else {
				colors.put(device, null);
			}
		});
		return colors;
	}
	
	public void setColor(Color color) {
		if (customEffectDisplay != null &&
				customEffectDisplay.isRunning()) {
			customEffectDisplay.stop();
		}
		
		if (panels != null) {
			group.forEach((device) -> {
				if (panels.get(device) != null) {
					for (Panel p : panels.get(device)) {
						panelColors.put(p.getId(), color);
					}
				}
			});
		}
		repaint();
	}
	
	public void setColor(Color color, NanoleafDevice device) {
		if (customEffectDisplay != null &&
				customEffectDisplay.isRunning()) {
			customEffectDisplay.stop();
		}
		
		if (panels != null) {
			if (panels.get(device) != null) {
				for (Panel p : panels.get(device)) {
					panelColors.put(p.getId(), color);
				}
			}
		}
		repaint();
	}
	
	public void setHSB(float h, float s, float b) {
		Color c = new Color(Color.HSBtoRGB(h/360, s/100, b/100));
		setColor(c);
	}
	
	public void setPanelColor(Panel panel, Color color) {
		if (customEffectDisplay != null &&
				customEffectDisplay.isRunning()) {
			customEffectDisplay.stop();
		}
		group.forEach((device) -> {
			if (panels.get(device) != null) {
				for (Panel p : panels.get(device)) {
					panelColors.put(p.getId(), color);
				}
			}
		});
		repaint();
	}
	
	public void setStaticEffect(CustomEffect ef, NanoleafDevice device) {
		if (device == null || panels == null) {
			return;
		}
		AnimationParser parser = new AnimationParser(ef);
		for (Panel p : panels.get(device)) {
			List<Frame> frames = parser.getFrames(p);
			if (frames != null && !frames.isEmpty()) {
				Frame f = parser.getFrames(p).get(0);
				new Thread(() -> {
					try {
						transitionToColor(p, new Color(f.getRed(), f.getGreen(), f.getBlue()), f.getTransitionTime()*10);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
			}
		}
		repaint();
	}
	
	public void transitionToColor(Panel p, Color color, int time)
			throws InterruptedException {
		final int NUM_COLORS = 300;
		Color panelColor = panelColors.get(p.getId());
		int deltaRed = color.getRed() - panelColor.getRed();
		int deltaGreen = color.getGreen() - panelColor.getGreen();
		int deltaBlue = color.getBlue() - panelColor.getBlue();
		
		if (deltaRed != 0 || deltaGreen != 0 || deltaBlue != 0) {
			for (int i = 0; i < NUM_COLORS; i++) {
				int red = panelColor.getRed() + ((deltaRed * i) / NUM_COLORS);
				int green = panelColor.getGreen() + ((deltaGreen * i) / NUM_COLORS);
				int blue = panelColor.getBlue() + ((deltaBlue * i) / NUM_COLORS);
				
				if (red < 0 || red > 255 ||
						green < 0 || green > 255 ||
						blue < 0 || blue > 255) {
					break;
				}
				
				panelColors.put(p.getId(), new Color(red, green, blue));
				repaint();
				Thread.sleep(time/30);
			}
		}
	}
	
	public void transitionAllPanelsToColor(NanoleafDevice device, Color color, int time)
			throws InterruptedException {
		final int NUM_COLORS = 300;
//		Color panelColor = panelColors.get(p.getId());
		Map<Integer, Integer> deltaRed = new HashMap<Integer, Integer>();
		Map<Integer, Integer> deltaGreen = new HashMap<Integer, Integer>();
		Map<Integer, Integer> deltaBlue = new HashMap<Integer, Integer>();
		for (Panel p : panels.get(device)) {
			deltaRed.put(p.getId(), color.getRed() - panelColors.get(p.getId()).getRed());
			deltaGreen.put(p.getId(), color.getGreen() - panelColors.get(p.getId()).getGreen());
			deltaBlue.put(p.getId(), color.getBlue() - panelColors.get(p.getId()).getBlue());
		}
//		int deltaRed = color.getRed() - panelColor.getRed();
//		int deltaGreen = color.getGreen() - panelColor.getGreen();
//		int deltaBlue = color.getBlue() - panelColor.getBlue();
		
		for (int i = 0; i < NUM_COLORS; i++) {
//			for (Panel p : panels.get(device)) {
			for (int j = 0; j < 1; j++) {
				Panel p = panels.get(device).get(j);
				int deltaRedP = deltaRed.get(p.getId());
				int deltaGreenP = deltaGreen.get(p.getId());
				int deltaBlueP = deltaBlue.get(p.getId());
				if (deltaRedP != 0 || deltaGreenP != 0 || deltaBlueP != 0) {
					int red = panelColors.get(p.getId()).getRed() + ((deltaRedP * i) / NUM_COLORS);
					int green = panelColors.get(p.getId()).getGreen() + ((deltaGreenP * i) / NUM_COLORS);
					int blue = panelColors.get(p.getId()).getBlue() + ((deltaBlueP * i) / NUM_COLORS);
					
					if (red < 0 || red > 255 ||
							green < 0 || green > 255 ||
							blue < 0 || blue > 255) {
						break;
					}
					
					
					panelColors.put(p.getId(), new Color(red, green, blue));
					System.out.println(new Color(red, green, blue));
//					repaint();
//					Thread.sleep(time/30);
				}
			}
			repaint();
			Thread.sleep(time/30);
		}
//		if (deltaRed != 0 || deltaGreen != 0 || deltaBlue != 0) {
//			for (int i = 0; i < NUM_COLORS; i++) {
//				int red = panelColor.getRed() + ((deltaRed * i) / NUM_COLORS);
//				int green = panelColor.getGreen() + ((deltaGreen * i) / NUM_COLORS);
//				int blue = panelColor.getBlue() + ((deltaBlue * i) / NUM_COLORS);
//				
//				if (red < 0 || red > 255 ||
//						green < 0 || green > 255 ||
//						blue < 0 || blue > 255) {
//					break;
//				}
//				
//				
//				panelColors.put(p.getId(), new Color(red, green, blue));
//				System.out.println(new Color(red, green, blue));
//				repaint();
//				Thread.sleep(time/30);
//			}
//		}
	}
	
	public Panel getCenterPanel(NanoleafDevice device) {
		return getPanelClosestToPoint(getCentroid(device), device);
	}
	
	public Point getCentroid(NanoleafDevice device) {
		int centroidX = 0, centroidY = 0;
		int numXPoints = 0, numYPoints = 0;
		List<Integer> xpoints = new ArrayList<Integer>();
		List<Integer> ypoints = new ArrayList<Integer>();
		
		for (Panel p : panels.get(device)) {
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
	
	public Point getCentroidFromPanels(List<Panel> panels) {
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
	
	private Panel getPanelClosestToPoint(Point point, NanoleafDevice device) {
		List<Panel> localPanels = panels.get(device);
		Panel closest = localPanels.get(0);
		double closestAmount = distanceToPanel(point, closest);
		for (Panel p : localPanels) {
			double d = distanceToPanel(point, p);
			if (d < closestAmount) {
				closest = p;
				closestAmount = d;
			}
		}
		return closest;
	}
	
	private double distanceToPanel(Point point, Panel panel) {
		return point.distance(panel.getX(), panel.getY());
	}
	
	private void getDefaultPanelPositions(List<Panel> locations, NanoleafDevice device) {
		Panel firstPanel = getCenterPanel(device);
		int offX = (getWidth()/2) - firstPanel.getX();
		int offY = (getHeight()/2) - firstPanel.getY();
		for (Panel location : locations) {
			int x = location.getX();
			int y = location.getY();
			location.setX(x + offX);
			location.setY(y + offY);
		}
	}
	
	private void drawTransformedPanel(Polygon panel, NanoleafDevice device, Graphics2D g2d) {
		AffineTransform original = g2d.getTransform();
		try {
			AffineTransform scaled = new AffineTransform();
			Point centroid = getCentroid(device);
			scaled.translate(centroid.getX(), centroid.getY());
			scaled.scale(scaleFactor, scaleFactor);
			scaled.translate(-centroid.getX(), -centroid.getY());
			Point tempCentroid = getCentroidFromPanels(pdl.getTempPanels().get(device));
			scaled.rotate(Math.toRadians(tempRotation.get(device)),
					tempCentroid.x, tempCentroid.y);
			g2d.setTransform(scaled);
			g2d.drawPolygon(panel);
		}
		finally {
			g2d.setTransform(original);
		}
	}
	
	private void fillTransformedPanel(Polygon panel, NanoleafDevice device, Graphics2D g2d) {
		AffineTransform original = g2d.getTransform();
		try {
			AffineTransform scaled = new AffineTransform();
			Point centroid = getCentroid(device);
			scaled.translate(centroid.getX(), centroid.getY());
			scaled.scale(scaleFactor, scaleFactor);
			scaled.translate(-centroid.getX(), -centroid.getY());
			Point tempCentroid = getCentroidFromPanels(pdl.getTempPanels().get(device));
			scaled.rotate(Math.toRadians(tempRotation.get(device)),
					tempCentroid.x, tempCentroid.y);
			g2d.setTransform(scaled);
			g2d.fillPolygon(panel);
		}
		finally {
			g2d.setTransform(original);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		initBuffer();
		super.paintComponent(buffG);
		
		final Graphics2D g2d = (Graphics2D)buffG;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw the transparent background
		buffG.setColor(new Color(0, 0, 0, 187));
		buffG.fillRect(0, 0, getWidth(), getHeight());
		
		if (initialized) {
			group.forEach((device) -> {
				List<Panel> localPanels = panels.get(device);
				Point localOffset = panelOffset.get(device);
				int localRotation = rotation.get(device);
				try {
					// Draw the panels
					for (Panel panel : localPanels) {
						if (panel.getId() == 0) {
							continue; // do not draw controller
						}
						int x = panel.getX() + localOffset.x;
						int y = panel.getY() + localOffset.y;
						int o = panel.getOrientation();
						
						buffG.setColor(panelColors.get(panel.getId()));
						
						if (device instanceof Aurora) {
							// Create the AURORA panel outline shapes (regular and inverted)
							Polygon tri = new Polygon();
							if (o == 0 || Math.abs(o) % 120 == 0) {
								tri = new UprightPanel(x, y, localRotation);
							}
							else {
								tri = new InvertedPanel(x, y, localRotation);
							}
							fillTransformedPanel(tri, device, g2d);
							buffG.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(4));
							drawTransformedPanel(tri, device, g2d);
							g2d.setStroke(new BasicStroke(1));
						}
						else if (device instanceof Canvas) {
							// Create the CANVAS panel outline shape
							SquarePanel sq = new SquarePanel(x, y, localRotation);
							fillTransformedPanel(sq, device, g2d);
							buffG.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(4));
							drawTransformedPanel(sq, device, g2d);
							g2d.setStroke(new BasicStroke(1));
						}
						else if (device instanceof Shapes) {
							// Create the SHAPES (hexagon) panel outline shape
							HexagonPanel hex = new HexagonPanel(x, y, localRotation);
							fillTransformedPanel(hex, device, g2d);
							buffG.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(4));
							drawTransformedPanel(hex, device, g2d);
							g2d.setStroke(new BasicStroke(1));
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
			
//			if (deviceType != DeviceType.AURORA && deviceType != DeviceType.CANVAS)
//			{
//				stopLoadingSpinner();
//				displayMessage("Error while loading preview. Your device may not be supported.",
//						Color.WHITE, new Font("Tahoma", Font.PLAIN, 20), buffG);
//			}
		}
		
		g.drawImage(buff, 0, 0, this);
	}
	
	private void displayMessage(String message, Color color, Font font, Graphics g) {
		buffG.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		buffG.setColor(color);
		buffG.drawString(message, (getWidth() - fm.stringWidth(message))/2,
				(getHeight() + fm.getHeight())/2);
	}
	
	private void initBuffer() {
		buff = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		buffG = buff.getGraphics();
	}
}
