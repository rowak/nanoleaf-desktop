package io.github.rowak.ui.panel.panelcanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JPanel;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.ui.dialog.LoadingSpinner;
import io.github.rowak.ui.dialog.TextDialog;
import io.github.rowak.Panel;
import io.github.rowak.Effect;

public class PanelCanvas extends JPanel
{
	private Aurora aurora;
	private Panel[] panels;
	private HashMap<Panel, Point> panelLocations;
	private CustomEffectDisplay customEffectDisplay;
	private LoadingSpinner spinner;
	
	public PanelCanvas(Aurora aurora)
	{
		this.aurora = aurora;
		initCanvas();
	}
	
	public void initCanvas()
	{
		try
		{
			panels = aurora.panelLayout().getPanels();
		}
		catch (NullPointerException npe)
		{
			panels = new Panel[0];
		}
		catch (StatusCodeException sce)
		{
			new TextDialog(this, "Could not connect to the Aurora. " +
					"Please try again.").setVisible(true);
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
		if (aurora == null)
		{
			spinner = new LoadingSpinner(Color.DARK_GRAY);
			add(spinner);
		}
	}
	
	private void stopLoadingSpinner()
	{
		if (aurora != null && this.isAncestorOf(spinner))
		{
			remove(spinner);
		}
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
	}
	
	public Aurora getAurora()
	{
		return this.aurora;
	}
	
	public Panel[] getPanels()
	{
		return this.panels;
	}
	
	public void checkAuroraState() throws StatusCodeException
	{
		if (aurora != null)
		{
			stopLoadingSpinner();
			
			String colorMode = aurora.state().getColorMode();
			if (colorMode.equals("hs") || colorMode.equals("ct"))
			{
				int hue = aurora.state().getHue();
				int sat = aurora.state().getSaturation();
				int bri = aurora.state().getBrightness();
				setHSB(hue, sat, bri);
			}
			else if (colorMode.equals("effects"))
			{
				String currentEffectName = aurora.effects().getCurrentEffectName();
				Effect currentEffect = aurora.effects().getCurrentEffect();
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
			if (aurora != null)
			{
				Color c = aurora.state().getOn() ?
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
			new TextDialog(this, "Lost connection to the Aurora. " +
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
		// Animation data parser
		String animData = ef.getAnimData();
		String[] dataTemp = animData.split(" ");
		int[] data = new int[dataTemp.length-1];
		for (int i = 1; i < dataTemp.length; i++)
		{
			data[i-1] = Integer.parseInt(dataTemp[i]);
		}
		
		int panelNum = 1;
		for (int i = 0; i < data.length; i+=7)
		{
			int panelId = data[i];
			int r = data[i+2];
			int g = data[i+3];
			int b = data[i+4];
			int w = data[i+5];
			
			float[] hsb = new float[3];
			Color.RGBtoHSB(r, g, b, hsb);
			Color rgb = null;
			try
			{
				rgb = new Color(Color.HSBtoRGB(hsb[0], hsb[1],
						aurora.state().getBrightness()/100f));
			}
			catch (StatusCodeException sce)
			{
				new TextDialog(this, "Lost connection to the Aurora. " +
						"Please try again.").setVisible(true);
			}
			r = rgb.getRed();
			g = rgb.getGreen();
			b = rgb.getBlue();
			
			if (panelNum <= panels.length)
			{
				getPanelById(panelId).setRGBW(r, g, b, w);
			}
			panelNum++;
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
	
	private Panel getPanelById(int id)
	{
		for (Panel p : panels)
		{
			if (p.getId() == id)
			{
				return p;
			}
		}
		return null;
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
		
//		// Fill in border
//		Insets insets = getBorder().getBorderInsets(this);
//		g.setColor(Color.DARK_GRAY);
//		g.fillRect(0, 0, getWidth(), insets.top);
		
		if (aurora != null)
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
				
				// Create the panel outline shapes (regular and inverted)
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
}
