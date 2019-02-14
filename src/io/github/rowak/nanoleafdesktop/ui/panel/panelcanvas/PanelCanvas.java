package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.Panel;
import io.github.rowak.StaticAnimDataParser;
import io.github.rowak.Effect;
import io.github.rowak.Frame;

public class PanelCanvas extends JPanel
{
	private int rotation;
	private Aurora device;
	private Panel[] panels;
	private DeviceType deviceType;
	private Map<Panel, Point> originalPanelLocations;
	private Map<Panel, Point> panelLocations;
	private CustomEffectDisplay customEffectDisplay;
	private LoadingSpinner spinner;
	
	public PanelCanvas(Aurora device)
	{
		this.device = device;
		if (device != null)
		{
			initCanvas();
		}
		startLoadingSpinner();
	}
	
	public enum DeviceType
	{
		AURORA, CANVAS
	}
	
	public void initCanvas()
	{
		try
		{
			panels = device.panelLayout().getPanels();
			setDeviceType();
		}
		catch (NullPointerException npe)
		{
			panels = new Panel[0];
		}
		catch (HttpRequestException hre)
		{
			new TextDialog(this, "Could not connect to the device. " +
					"Please relaunch the application.").setVisible(true);
		}
		catch (StatusCodeException sce)
		{
			new TextDialog(this, "An error occurred while getting data from the device. " +
					"Please relaunch the application.").setVisible(true);
		}
		if (device != null)
		{
			panelLocations = new HashMap<Panel, Point>();
			for (Panel p : panels)
			{
				panelLocations.put(p, new Point(p.getX() + getWidth()/2,
						-p.getY() + getHeight()/2));
			}
			panelLocations = getDefaultPanelPositions(panelLocations);
			originalPanelLocations = new HashMap<Panel, Point>(panelLocations);
			
			loadUserPanelRotation();
			
			customEffectDisplay = new CustomEffectDisplay(this);
			toggleOn();
			PanelDragListener pdl = new PanelDragListener(this, panels, panelLocations);
			addMouseListener(pdl);
			addMouseMotionListener(pdl);
		}
	}
	
	private void loadUserPanelRotation()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String defaultRotation = manager.getProperty("panelRotation");
		if (defaultRotation != null)
		{
			rotatePanels(Integer.parseInt(defaultRotation));
		}
	}
	
	private void startLoadingSpinner()
	{
		if (device == null)
		{
			spinner = new LoadingSpinner(Color.DARK_GRAY);
			add(spinner);
		}
	}
	
	private void stopLoadingSpinner()
	{
		if (device != null && this.isAncestorOf(spinner))
		{
			remove(spinner);
		}
	}
	
	private void setDeviceType()
	{
		if (device.getName().toLowerCase().contains("light panels") ||
				device.getName().toLowerCase().contains("aurora"))
		{
			deviceType = DeviceType.AURORA;
		}
		else if (device.getName().toLowerCase().contains("canvas"))
		{
			deviceType = DeviceType.CANVAS;
		}
	}
	
	public void setAurora(Aurora device)
	{
		this.device = device;
		setDeviceType();
		loadUserPanelRotation();
	}
	
	public Aurora getAurora()
	{
		return this.device;
	}
	
	public Panel[] getPanels()
	{
		return this.panels;
	}
	
	public int getRotation()
	{
		return this.rotation;
	}
	
	public void checkAuroraState() throws StatusCodeException
	{
		if (device != null)
		{
			stopLoadingSpinner();
			
			String colorMode = device.state().getColorMode();
			if (colorMode.equals("hs") || colorMode.equals("ct"))
			{
				customEffectDisplay.stop();
				
				int hue = device.state().getHue();
				int sat = device.state().getSaturation();
				int bri = device.state().getBrightness();
				setHSB(hue, sat, bri);
			}
			else if (colorMode.equals("effects"))
			{
				String currentEffectName = device.effects().getCurrentEffectName();
				Effect currentEffect = device.effects().getCurrentEffect();
				if (currentEffect != null && currentEffect.getAnimType() != null)
				{
					if (currentEffectName.equals("*Static*") ||
							currentEffect.getAnimType().equals(Effect.Type.STATIC))
					{
						customEffectDisplay.stop();
						setStaticEffect(currentEffect);
					}
					else if (currentEffect.getAnimType().equals(Effect.Type.CUSTOM))
					{
						customEffectDisplay.changeEffect(currentEffect);
					}
					else if (!currentEffect.getAnimType().equals(Effect.Type.STATIC))
					{
						customEffectDisplay.stop();
						io.github.rowak.Color[] palette =
								currentEffect.getPalette();
						int[] avgRgb = new int[3];
						if (palette != null)
						{
							for (io.github.rowak.Color c : palette)
							{
								Color rgb = new Color(Color.HSBtoRGB(c.getHue()/360f,
										c.getSaturation()/100f, c.getBrightness()/100f));
								avgRgb[0] += rgb.getRed();
								avgRgb[1] += rgb.getGreen();
								avgRgb[2] += rgb.getBlue();
							}
							Color avgColor = new Color(avgRgb[0]/palette.length,
									avgRgb[1]/palette.length, avgRgb[2]/palette.length);
							setColor(avgColor);
						}
					}
				}
			}
		}
	}
	
	public void toggleOn()
	{
		try
		{
			if (device != null)
			{
				Color c = device.state().getOn() ?
						Color.WHITE : Color.BLACK;
				setColor(c);
				
				if (!c.equals(Color.BLACK))
				{
					checkAuroraState();
				}
			}
		}
		catch (StatusCodeException sce)
		{
			new TextDialog(this, "Lost connection to the device. " +
					"Please try again.").setVisible(true);
		}
	}
	
	public Color getColor()
	{
		boolean same = true;
		Color first = new Color(panels[0].getRed(),
				panels[0].getGreen(), panels[0].getBlue());
		for (Panel p : panels)
		{
			Color next = new Color(p.getRed(),
					p.getGreen(), p.getBlue());
			if (!first.equals(next))
			{
				same = false;
				break;
			}
		}
		if (same)
		{
			return first;
		}
		return null;
	}
	
	public void setColor(Color color)
	{
		if (customEffectDisplay.isRunning())
		{
			customEffectDisplay.stop();
		}
		
		for (Panel p : panels)
		{
			p.setRGBW(color.getRed(), color.getGreen(),
					color.getBlue(), 0);
		}
		repaint();
	}
	
	public void setHSB(float h, float s, float b)
	{
		Color c = new Color(Color.HSBtoRGB(h/360, s/100, b/100));
		setColor(c);
	}
	
	public void setPanelColor(Panel panel, Color color)
	{
		if (customEffectDisplay.isRunning())
		{
			customEffectDisplay.stop();
		}
		for (Panel p : panels)
		{
			if (p.getId() == panel.getId())
			{
				p.setRGBW(color.getRed(), color.getGreen(),
						color.getBlue(), 0);
				break;
			}
		}
		repaint();
	}
	
	public void setStaticEffect(Effect ef)
	{
		StaticAnimDataParser sadp = new StaticAnimDataParser(ef);
		for (Panel p : panels)
		{
			Frame f = sadp.getFrame(p);
			p.setRGB(f.getRed(), f.getGreen(), f.getBlue());
		}
		repaint();
	}
	
	public void transitionToColor(Panel p, Color color, int time)
			throws InterruptedException
	{
		final int NUM_COLORS = 300;
		int deltaRed = color.getRed() - p.getRed();
		int deltaGreen = color.getGreen() - p.getGreen();
		int deltaBlue = color.getBlue() - p.getBlue();
		
		if (deltaRed != 0 || deltaGreen != 0 || deltaBlue != 0)
		{
			for (int i = 0; i < NUM_COLORS; i++)
			{
				int red = p.getRed() + ((deltaRed * i) / NUM_COLORS);
				int green = p.getGreen() + ((deltaGreen * i) / NUM_COLORS);
				int blue = p.getBlue() + ((deltaBlue * i) / NUM_COLORS);
				
				if (red < 0 || red > 255 ||
						green < 0 || green > 255 ||
						blue < 0 || blue > 255)
				{
					break;
				}
				
				p.setRGB(red, green, blue);
				repaint();
				Thread.sleep(time/30);
			}
		}
	}
	
	public void rotatePanels(int angle)
	{
		Point origin = new Point(getWidth()/2, getHeight()/2);
		double radAngle = Math.toRadians(angle);
		
		for (Panel p : panels)
		{
			Point loc = originalPanelLocations.get(p);
			int x = loc.x - origin.x;
			int y = loc.y - origin.y;
			
			double newX = x * Math.cos(radAngle) - y * Math.sin(radAngle);
			double newY = x * Math.sin(radAngle) + y * Math.cos(radAngle);
			
			x = (int)(newX + origin.x);
			y = (int)(newY + origin.y);
			panelLocations.put(p, new Point(x, y));
		}
		
		rotation = angle;
	}
	
	public Panel getCenterPanel()
	{
		return getPanelClosestToPoint(getCentroid());
	}
	
	public Point getCentroid()
	{
		int centroidX = 0, centroidY = 0;
		int numXPoints = 0, numYPoints = 0;
		List<Integer> xpoints = new ArrayList<Integer>();
		List<Integer> ypoints = new ArrayList<Integer>();
		
		for (Panel p : panels)
		{
			int x = panelLocations.get(p).x;
			int y = panelLocations.get(p).y;
			if (!xpoints.contains(x))
			{
				centroidX += x;
				xpoints.add(x);
				numXPoints++;
			}
			if (!ypoints.contains(y))
			{
				centroidY += y;
				ypoints.add(y);
				numYPoints++;
			}
		}
		centroidX /= numXPoints;
		centroidY /= numYPoints;
		return new Point(centroidX, centroidY);
	}
	
	private Panel getPanelClosestToPoint(Point point)
	{
		Panel closest = panels[0];
		double closestAmount = distanceToPanel(point, panels[0]);
		for (Panel p : panels)
		{
			double d = distanceToPanel(point, p);
			if (d < closestAmount)
			{
				closest = p;
				closestAmount = d;
			}
		}
		return closest;
	}
	
	private double distanceToPanel(Point point, Panel panel)
	{
		int panelx = panelLocations.get(panel).x;
		int panely = panelLocations.get(panel).y;
		return point.distance(panelx, panely);
	}
	
	private Map<Panel, Point> getDefaultPanelPositions(Map<Panel, Point> locations)
	{
		Map<Panel, Point> newLocations = new HashMap<Panel, Point>(panelLocations);
		Panel firstPanel = getCenterPanel();
		int offX = (getWidth()/2) - newLocations.get(firstPanel).x;
		int offY = (getHeight()/2) - newLocations.get(firstPanel).y;
		for (Panel p : panels)
		{
			int x = newLocations.get(p).x;
			int y = newLocations.get(p).y;
			newLocations.get(p).setLocation(x + offX, y + offY);
		}
		return newLocations;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw the transparent background
		g.setColor(new Color(0, 0, 0, 187));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (device != null)
		{
			// Draw the panels
			for (Panel panel : panels)
			{
				int x = panelLocations.get(panel).x;
				int y = panelLocations.get(panel).y;
				int o = panel.getOrientation();
				
				// Draw the "centroids" (these are invisible, since
				// the panel colors are drawn after this line
				g.drawRect(x, y, 2, 2);
				
				if (deviceType == DeviceType.AURORA)
				{
					// Create the AURORA panel outline shapes (regular and inverted)
					Polygon tri = new Polygon();
					if (o == 0 || Math.abs(o) % 120 == 0)
					{
						tri = new UprightPanel(x, y, this);
					}
					else
					{
						tri = new InvertedPanel(x, y, this);
					}
					g.setColor(new Color(panel.getRed(),
							panel.getGreen(), panel.getBlue()));
					g.fillPolygon(tri);
					g.setColor(Color.BLACK);
					g2d.setStroke(new BasicStroke(4));
					g.drawPolygon(tri);
					g2d.setStroke(new BasicStroke(1));
				}
				else if (deviceType == DeviceType.CANVAS)
				{
					// Create the CANVAS panel outline shape
					SquarePanel sq = new SquarePanel(x, y, this);
					g.setColor(new Color(panel.getRed(),
							panel.getGreen(), panel.getBlue()));
					g.fillPolygon(sq);
					g.setColor(Color.BLACK);
					g2d.setStroke(new BasicStroke(4));
					g.drawPolygon(sq);
					g2d.setStroke(new BasicStroke(1));
				}
			}
		}
	}
}
