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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.Panel;
import io.github.rowak.StaticAnimDataParser;
import io.github.rowak.Effect;
import io.github.rowak.Frame;

public class PanelCanvas extends JPanel
{
	private boolean initialized;
	private int[] rotation, tempRotation;
	private float scaleFactor = 1f;
	private Point[] panelOffset;
	private Aurora[] devices;
	private Panel[][] panels;
	private DeviceType deviceType;
	private PanelActionListener pdl;
	private CustomEffectDisplay customEffectDisplay;
	private LoadingSpinner spinner;
	private Graphics buffG;
	private BufferedImage buff;
	
	public PanelCanvas(Aurora[] devices)
	{
		this.devices = devices;
		if (devices != null && devices.length == 1)
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
		if (!initialized && this.isValid() &&
				devices != null && panels == null)
		{
			try
			{
				panels = new Panel[devices.length][];
				for (int i = 0; i < devices.length; i++)
				{
					panels[i] = devices[i].panelLayout().getPanelsRotated();
				}
				panelOffset = new Point[devices.length];
				for (int i = 0; i < panelOffset.length; i++)
				{
					panelOffset[i] = new Point();
				}
				rotation = new int[devices.length];
				for (int i = 0; i < rotation.length; i++)
				{
					rotation[i] = 360 - devices[i].panelLayout().getGlobalOrientation();
				}
				tempRotation = new int[devices.length];
				setDeviceType();
			}
			catch (NullPointerException npe)
			{
				panels = new Panel[0][];
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
			
			for (int i = 0; i < panels.length; i++)
			{
				for (Panel p : panels[i])
				{
					p.setX(p.getX() + getWidth()/2);
					p.setY(-p.getY() + getHeight()/2);
				}
				getDefaultPanelPositions(panels[i], i);
			}
			
			customEffectDisplay = new CustomEffectDisplay(this);
			toggleOn();
			
			if (pdl != null)
			{
				removeMouseListener(pdl);
				removeMouseMotionListener(pdl);
				removeMouseWheelListener(pdl);
			}
			
			pdl = new PanelActionListener(
					this, panels, devices);
			addMouseListener(pdl);
			addMouseMotionListener(pdl);
			addMouseWheelListener(pdl);
			
			initialized = true;
		}
	}
	
	public void reinitialize()
	{
		initialized = false;
		panels = null;
		initCanvas();
	}
	
	private void startLoadingSpinner()
	{
		if (devices != null && devices[0] == null)
		{
			spinner = new LoadingSpinner(Color.DARK_GRAY);
			add(spinner);
		}
	}
	
	private void stopLoadingSpinner()
	{
		if (devices[0] != null && this.isAncestorOf(spinner))
		{
			remove(spinner);
		}
	}
	
	private void setDeviceType()
	{
		if (devices[0].getName().toLowerCase().contains("light panels") ||
				devices[0].getName().toLowerCase().contains("aurora"))
		{
			deviceType = DeviceType.AURORA;
		}
		else if (devices[0].getName().toLowerCase().contains("canvas"))
		{
			deviceType = DeviceType.CANVAS;
		}
	}
	
	public void setAuroras(Aurora[] devices)
	{
		this.devices = devices;
		reinitialize();
		if (devices.length == 1)
		{
			setDeviceType();
		}
	}
	
	public Point[] getPanelOffset()
	{
		return panelOffset;
	}
	
	public void setPanelOffset(Point[] panelOffset)
	{
		this.panelOffset = panelOffset;
	}
	
	public Aurora[] getAuroras()
	{
		return devices;
	}
	
	public Panel[] getPanels(int deviceIndex)
	{
		return panels[deviceIndex];
	}
	
	public void setPanels(Panel[][] panels)
	{
		this.panels = panels;
	}
	
	public Panel[] getGroupPanels()
	{
		List<Panel> groupPanels = new ArrayList<Panel>();
		for (int i = 0; i < panels.length; i++)
		{
			for (Panel p : panels[i])
			{
				int x = (p.getX() + panelOffset[i].x);
				int y = -(p.getY() + panelOffset[i].y);
				groupPanels.add(new Panel(p.getId(),
						x, y, p.getOrientation()));
			}
		}
		return groupPanels.toArray(new Panel[]{});
	}
	
	public int getRotation(int deviceIndex)
	{
		return rotation[deviceIndex];
	}
	
	public void setRotation(int degrees, int deviceIndex)
	{
		try
		{
			degrees += rotation[deviceIndex];
			if (degrees < 0)
			{
				degrees = 360 - Math.abs(degrees);
			}
			if (degrees > 360)
			{
				degrees = degrees % 360;
			}
			devices[deviceIndex].panelLayout()
				.setGlobalOrientation(360-degrees);
			rotation[deviceIndex] = degrees;
			tempRotation[deviceIndex] = 0;
			reloadPanels(deviceIndex);
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	public int getTempRotation(int deviceIndex)
	{
		return tempRotation[deviceIndex];
	}
	
	public void setTempRotation(int degrees, int deviceIndex)
	{
		tempRotation[deviceIndex] = degrees;
	}
	
	public float getScaleFactor()
	{
		return scaleFactor;
	}
	
	public void setScaleFactor(float factor)
	{
		scaleFactor = factor;
	}
	
	private void reloadPanels(int deviceIndex)
	{
		try
		{
			panels[deviceIndex] = devices[deviceIndex]
					.panelLayout().getPanelsRotated();
		}
		catch (NullPointerException npe)
		{
			panels[deviceIndex] = new Panel[0];
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
		
		for (Panel p : panels[deviceIndex])
		{
			p.setX(p.getX() + getWidth()/2);
			p.setY(-p.getY() + getHeight()/2);
		}
		getDefaultPanelPositions(
				panels[deviceIndex], deviceIndex);
	}
	
	public void checkAuroraState() throws StatusCodeException
	{
		if (devices != null && devices[0] != null)
		{
			stopLoadingSpinner();
			
			String colorMode = devices[0].state().getColorMode();
			if (colorMode.equals("hs") || colorMode.equals("ct"))
			{
				if (customEffectDisplay != null)
				{
					customEffectDisplay.stop();
				}
				
				int hue = devices[0].state().getHue();
				int sat = devices[0].state().getSaturation();
				int bri = devices[0].state().getBrightness();
				setHSB(hue, sat, bri);
			}
			else if (colorMode.equals("effects") || colorMode.equals("effect"))
			{
				String currentEffectName = devices[0].effects().getCurrentEffectName();
				Effect currentEffect = devices[0].effects().getCurrentEffect();
				if (currentEffect != null && currentEffect.getAnimType() != null)
				{
					if (currentEffectName.equals("*Static*") ||
							currentEffect.getAnimType().equals(Effect.Type.STATIC))
					{
						if (customEffectDisplay != null)
						{
							customEffectDisplay.stop();
						}
						setStaticEffect(currentEffect);
					}
					else if (currentEffect.getAnimType().equals(Effect.Type.CUSTOM))
					{
						// **************** Currently disabled ****************
						//customEffectDisplay.changeEffect(currentEffect);
					}
					else if (!currentEffect.getAnimType().equals(Effect.Type.STATIC))
					{
						if (customEffectDisplay != null)
						{
							customEffectDisplay.stop();
						}
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
			if (devices[0] != null)
			{
				Color c = devices[0].state().getOn() ?
						Color.WHITE : Color.BLACK;
				setColor(c);
				
				if (!c.equals(Color.BLACK))
				{
					checkAuroraState();
				}
				stopLoadingSpinner();
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
		Color first = new Color(panels[0][0].getRed(),
				panels[0][0].getGreen(), panels[0][0].getBlue());
		for (int i = 0; i < panels.length; i++)
		{
			for (Panel p : panels[i])
			{
				Color next = new Color(p.getRed(),
						p.getGreen(), p.getBlue());
				if (!first.equals(next))
				{
					same = false;
					break;
				}
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
		if (customEffectDisplay != null &&
				customEffectDisplay.isRunning())
		{
			customEffectDisplay.stop();
		}
		
		if (panels != null)
		{
			for (int i = 0; i < panels.length; i++)
			{
				if (panels[i] != null)
				{
					for (Panel p : panels[i])
					{
						p.setRGBW(color.getRed(), color.getGreen(),
								color.getBlue(), 0);
					}
				}
			}
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
		if (customEffectDisplay != null &&
				customEffectDisplay.isRunning())
		{
			customEffectDisplay.stop();
		}
		for (int i = 0; i < panels.length; i++)
		{
			for (Panel p : panels[i])
			{
				if (p.getId() == panel.getId())
				{
					p.setRGBW(color.getRed(), color.getGreen(),
							color.getBlue(), 0);
					break;
				}
			}
		}
		repaint();
	}
	
	public void setStaticEffect(Effect ef)
	{
		StaticAnimDataParser sadp = new StaticAnimDataParser(ef);
		for (int i = 0; i < panels.length; i++)
		{
			for (Panel p : panels[i])
			{
				Frame f = sadp.getFrame(p);
				p.setRGB(f.getRed(), f.getGreen(), f.getBlue());
			}
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
	
	public Panel getCenterPanel(int deviceIndex)
	{
		return getPanelClosestToPoint(getCentroid(deviceIndex), deviceIndex);
	}
	
	public Point getCentroid(int deviceIndex)
	{
		int centroidX = 0, centroidY = 0;
		int numXPoints = 0, numYPoints = 0;
		List<Integer> xpoints = new ArrayList<Integer>();
		List<Integer> ypoints = new ArrayList<Integer>();
		
		for (Panel p : panels[0])
		{
			int x = p.getX();
			int y = p.getY();
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
	
	public Point getCentroidFromPanels(int deviceIndex, Panel[] panels)
	{
		int centroidX = 0, centroidY = 0;
		int numXPoints = 0, numYPoints = 0;
		List<Integer> xpoints = new ArrayList<Integer>();
		List<Integer> ypoints = new ArrayList<Integer>();
		
		for (Panel p : panels)
		{
			int x = p.getX();
			int y = p.getY();
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
	
	private Panel getPanelClosestToPoint(Point point, int deviceIndex)
	{
		Panel closest = panels[deviceIndex][0];
		double closestAmount = distanceToPanel(point, panels[deviceIndex][0]);
		for (Panel p : panels[deviceIndex])
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
		int panelx = panel.getX();
		int panely = panel.getY();
		return point.distance(panelx, panely);
	}
	
	private void getDefaultPanelPositions(Panel[] locations, int deviceIndex)
	{
		Panel firstPanel = getCenterPanel(deviceIndex);
		int offX = (getWidth()/2) - firstPanel.getX();
		int offY = (getHeight()/2) - firstPanel.getY();
		for (int i = 0; i < locations.length; i++)
		{
			int x = locations[i].getX();
			int y = locations[i].getY();
			locations[i].setX(x + offX);
			locations[i].setY(y + offY);
		}
	}
	
	private void drawTransformedPanel(Polygon panel, int deviceIndex, Graphics2D g2d)
	{
		AffineTransform original = g2d.getTransform();
		try
		{
			AffineTransform scaled = new AffineTransform();
			Point centroid = getCentroid(deviceIndex);
			scaled.translate(centroid.getX(), centroid.getY());
			scaled.scale(scaleFactor, scaleFactor);
			scaled.translate(-centroid.getX(), -centroid.getY());
			Point tempCentroid = getCentroidFromPanels(deviceIndex,
					pdl.getTempPanels()[deviceIndex]);
			scaled.rotate(Math.toRadians(tempRotation[deviceIndex]),
					tempCentroid.x, tempCentroid.y);
			g2d.setTransform(scaled);
			g2d.drawPolygon(panel);
		}
		finally
		{
			g2d.setTransform(original);
		}
	}
	
	private void fillTransformedPanel(Polygon panel, int deviceIndex, Graphics2D g2d)
	{
		AffineTransform original = g2d.getTransform();
		try
		{
			AffineTransform scaled = new AffineTransform();
			Point centroid = getCentroid(deviceIndex);
			scaled.translate(centroid.getX(), centroid.getY());
			scaled.scale(scaleFactor, scaleFactor);
			scaled.translate(-centroid.getX(), -centroid.getY());
			Point tempCentroid = getCentroidFromPanels(deviceIndex,
					pdl.getTempPanels()[deviceIndex]);
			scaled.rotate(Math.toRadians(tempRotation[deviceIndex]),
					tempCentroid.x, tempCentroid.y);
			g2d.setTransform(scaled);
			g2d.fillPolygon(panel);
		}
		finally
		{
			g2d.setTransform(original);
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		initBuffer();
		super.paintComponent(buffG);
		
		Graphics2D g2d = (Graphics2D)buffG;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw the transparent background
		buffG.setColor(new Color(0, 0, 0, 187));
		buffG.fillRect(0, 0, getWidth(), getHeight());
		
		if (initialized)
		{
			for (int d = 0; d < devices.length; d++)
			{
				try
				{
					// Draw the panels
					for (int i = 0; i < panels[d].length; i++)
					{
						Panel panel = panels[d][i];
						int x = panel.getX() + panelOffset[d].x;
						int y = panel.getY() + panelOffset[d].y;
						int o = panel.getOrientation();
						
						if (deviceType == DeviceType.AURORA)
						{
							// Create the AURORA panel outline shapes (regular and inverted)
							Polygon tri = new Polygon();
							if (o == 0 || Math.abs(o) % 120 == 0)
							{
								tri = new UprightPanel(x, y, rotation[d]);
							}
							else
							{
								tri = new InvertedPanel(x, y, rotation[d]);
							}
							buffG.setColor(new Color(panel.getRed(),
									panel.getGreen(), panel.getBlue()));
							fillTransformedPanel(tri, d, g2d);
							buffG.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(4));
							drawTransformedPanel(tri, d, g2d);
							g2d.setStroke(new BasicStroke(1));
						}
						else if (deviceType == DeviceType.CANVAS)
						{
							// Create the CANVAS panel outline shape
							SquarePanel sq = new SquarePanel(x, y, rotation[d]);
							buffG.setColor(new Color(panel.getRed(),
									panel.getGreen(), panel.getBlue()));
							fillTransformedPanel(sq, d, g2d);
							buffG.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(4));
							drawTransformedPanel(sq, d, g2d);
							g2d.setStroke(new BasicStroke(1));
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			if (deviceType != DeviceType.AURORA && deviceType != DeviceType.CANVAS)
			{
				displayMessage("Error while loading preview. Your device may not be supported.",
						Color.WHITE, new Font("Tahoma", Font.PLAIN, 20), buffG);
			}
		}
		
		g.drawImage(buff, 0, 0, this);
	}
	
	private void displayMessage(String message, Color color, Font font, Graphics g)
	{
		buffG.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		buffG.setColor(color);
		buffG.drawString(message, (getWidth() - fm.stringWidth(message))/2,
				(getHeight() + fm.getHeight())/2);
	}
	
	private void initBuffer()
	{
		buff = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		buffG = buff.getGraphics();
	}
}
