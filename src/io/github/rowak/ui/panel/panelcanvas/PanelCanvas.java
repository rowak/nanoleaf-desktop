package io.github.rowak.ui.panel.panelcanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JPanel;

import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.ui.dialog.LoadingSpinner;
import io.github.rowak.ui.dialog.TextDialog;
import io.github.rowak.Panel;
import io.github.rowak.StaticAnimDataParser;
import io.github.rowak.Effect;
import io.github.rowak.Frame;

public class PanelCanvas extends JPanel
{
	private Aurora device;
	private Panel[] panels;
	private DeviceType deviceType;
	private HashMap<Panel, Point> panelLocations;
	private CustomEffectDisplay customEffectDisplay;
	private LoadingSpinner spinner;
	
	public PanelCanvas(Aurora device)
	{
		this.device = device;
		initCanvas();
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
		panelLocations = new HashMap<Panel, Point>();
		final int DEFAULT_X_OFFSET = 150;
		final int DEFAULT_Y_OFFSET = 150;
		for (Panel p : panels)
		{
			panelLocations.put(p, new Point(p.getX() + getWidth()/2 + DEFAULT_X_OFFSET,
					-p.getY() + getHeight()/2 + DEFAULT_Y_OFFSET));
		}
		
		customEffectDisplay = new CustomEffectDisplay(this);
		toggleOn();
		PanelDragListener pdl = new PanelDragListener(this, panels, panelLocations);
		addMouseListener(pdl);
		addMouseMotionListener(pdl);
		
		startLoadingSpinner();
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
		if (device.getName().toLowerCase().contains("light panels"))
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
	}
	
	public Aurora getAurora()
	{
		return this.device;
	}
	
	public Panel[] getPanels()
	{
		return this.panels;
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
						tri = new UprightPanel(x, y);
					}
					else
					{
						tri = new InvertedPanel(x, y);
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
					Square sq = new Square(x, y);
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
	
	private class PanelDragListener extends MouseAdapter
	{
		private Panel[] panels;
		private HashMap<Panel, Point> panelLocations, tempPanelLocations;
		private Point mouseLast;
		private PanelCanvas canvas;
		
		public PanelDragListener(PanelCanvas canvas,
				Panel[] panels, HashMap<Panel, Point> panelLocations)
		{
			this.canvas = canvas;
			this.panels = panels;
			this.panelLocations = panelLocations;
			tempPanelLocations = new HashMap<Panel, Point>(panelLocations);
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			mouseLast = e.getPoint();
			canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			mouseLast = null;
			tempPanelLocations = new HashMap<Panel, Point>(panelLocations);
			canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (mouseLast != null)
			{
				Point mouse = e.getPoint();
				
				int xdiff = mouse.x - mouseLast.x;
				int ydiff = mouse.y - mouseLast.y;
				
				for (Panel p : panels)
				{
					int x = tempPanelLocations.get(p).x + xdiff;
					int y = tempPanelLocations.get(p).y + ydiff;
					panelLocations.put(p, new Point(x, y));
				}
				canvas.repaint();
			}
		}
	}
	
	private class UprightPanel extends Polygon
	{
		public UprightPanel(int x, int y)
		{
			// top
			addPoint(x + 7, y -60);
			addPoint(x -7, y -60);
			// bottom-left
			addPoint(x -60, y + 40);
			addPoint(x -55, y + 50);
			// bottom-right
			addPoint(x + 55, y + 50);
			addPoint(x + 60, y + 40);
		}
	}
	
	private class InvertedPanel extends Polygon
	{
		public InvertedPanel(int x, int y)
		{
			// top-left
			addPoint(x -55, y -15);
			addPoint(x -50, y -25);
			// top-right
			addPoint(x + 50, y -25);
			addPoint(x + 55, y -15);
			// bottom
			addPoint(x + 5, y + 80);
			addPoint(x -5, y + 80);
		}
	}
	
	private class Square extends Polygon
	{
		public Square(int x, int y)
		{
			//top-left
			addPoint(x, y);
			//top-right
			addPoint(x + 90, y);
			//bottom-right
			addPoint(x + 90, y + 90);
			//bottom-left
			addPoint(x, y + 90);
		}
	}
}
