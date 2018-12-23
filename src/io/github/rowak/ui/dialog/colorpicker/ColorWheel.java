package io.github.rowak.ui.dialog.colorpicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorWheel extends JPanel
{
	private int width, height;
	private WheelTracker tracker;
	
	public ColorWheel(int width, int height)
	{
		this.width = width;
		this.height = height;
		add(Box.createRigidArea(new Dimension(width, height)));
		setBackground(Color.DARK_GRAY);
		tracker = new WheelTracker();
	}
	
	public Color getColor()
	{
		return getColor(tracker.getX(), tracker.getY());
	}
	
	public void setColor(Color color)
	{
		tracker.setColor(color);
	}
	
	public void addChangeListener(ChangeListener listener)
	{
	    listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener)
	{
	    listenerList.remove(ChangeListener.class, listener);
	}

	public ChangeListener[] getChangeListeners()
	{
	    return listenerList.getListeners(ChangeListener.class);
	}

	protected void fireChangeListeners()
	{
	    ChangeEvent event = new ChangeEvent(this);
	    for (ChangeListener listener : getChangeListeners())
	    {
	        listener.stateChanged(event);
	    }
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		paintWheel(g);		
		paintWheelBorder(g);
		tracker.paint(g);
	}
	
	public void paintWheel(Graphics g)
	{
		/*
		 * Color wheel algorithm source:
		 * https://rosettacode.org/wiki/Color_wheel
		 */
		float centerX = width/2;
		float centerY = height/2;
		float radius = centerX;
		if (centerY < radius)
		{
			radius = centerY;
		}
		for (int y = 0; y < height; y++)
		{
			float dy = y - centerY;
			for (int x = 0; x < width; x++)
			{
				float dx = x - centerX;
				float dist = (float)Math.sqrt(dx*dx + dy*dy);
				if (dist <= radius)
				{
					float hue = (float)(((Math.atan2(dx, dy) / Math.PI) + 1f) / 2f);
					Color rgb = new Color(Color.HSBtoRGB(hue, 1f, 1f));
					g.setColor(rgb);
					g.drawRect(x, y, 1, 1);
				}
				else
				{
					g.setColor(getBackground());
					g.drawRect(x, y, 1, 1);
				}
			}
		}
	}
	
	public void paintWheelBorder(Graphics g)
	{
		float centerX = width/2;
		float centerY = height/2;
		float radius = centerX;
		for (int y = 0; y < height; y++)
		{
			float dy = y - centerY;
			for (int x = 0; x < width; x++)
			{
				float dx = x - centerX;
				int dist = (int)Math.ceil(Math.sqrt(dx*dx + dy*dy));
				if (dist == radius)
				{
					g.setColor(Color.GRAY);
					g.drawRect(x, y, 1, 1);
				}
			}
		}
	}
	
	private Color getColor(int x, int y)
	{
		float centerX = width/2;
		float centerY = height/2;
		float radius = centerX;
		if (centerY < radius)
		{
			radius = centerY;
		}
		for (int circy = 0; circy < height; circy++)
		{
			float dy = circy - centerY;
			for (int circx = 0; circx < width; circx++)
			{
				if (circx == x && circy == y)
				{
					float dx = circx - centerX;
					float dist = (float)Math.sqrt(dx*dx + dy*dy);
					if (dist <= radius)
					{
						float hue = (float)(((Math.atan2(dx, dy) / Math.PI) + 1f) / 2f);
						return new Color(Color.HSBtoRGB(hue, 1f, 1f));
					}
				}
			}
		}
		return null;
	}
	
	private class WheelTracker
	{
		private int x, y;
		private Color borderColor, trackerColor;
		
		public WheelTracker()
		{
			x = width/2;
			y = width/2;
			borderColor = Color.DARK_GRAY;
			trackerColor = Color.WHITE;

			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					updatePosition(e.getX(), e.getY());
				}
			});
			addMouseMotionListener(new MouseAdapter()
			{
				@Override
				public void mouseDragged(MouseEvent e)
				{
					updatePosition(e.getX(), e.getY());
				}
			});
		}
		
		private void updatePosition(int x, int y)
		{
			int centerX = ColorWheel.this.width/2;
			int centerY = ColorWheel.this.height/2;
			double radius = ColorWheel.this.width/2;
			double dist = Math.sqrt(Math.pow(x - centerX, 2) +
					Math.pow(y - centerY, 2));
			//double xdist = Math.sqrt(Math.pow(e.getX() - centerX, 2));
			//double ydist = Math.sqrt(Math.pow(e.getY() - centerY, 2));
			if (dist <= radius)
			{
				this.x = x;
				this.y = y;
				fireChangeListeners();
				repaint();
			}
		}
		
		public int getX()
		{
			return this.x;
		}
		
		public int getY()
		{
			return this.y;
		}
		
		public void setColor(Color color)
		{
			float centerX = width/2;
			float centerY = height/2;
			float radius = centerX;
			if (centerY < radius)
			{
				radius = centerY;
			}
			for (int y = 0; y < height; y++)
			{
				float dy = y - centerY;
				for (int x = 0; x < width; x++)
				{
					float dx = x - centerX;
					float dist = (float)Math.sqrt(dx*dx + dy*dy);
					if (dist <= radius)
					{
						float hue = (float)(((Math.atan2(dx, dy) / Math.PI) + 1f) / 2f);
						Color colorX = new Color(Color.HSBtoRGB(hue, 1f, 1f));
						
						if (colorX.equals(color))
						{
							this.x = x;
							this.y = y;
							repaint();
						}
					}
				}
			}
		}
		
		public void paint(Graphics g)
		{
			final int DIAMETER = 20;
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(trackerColor);
			g.fillOval(x - DIAMETER/2, y - DIAMETER/2, DIAMETER, DIAMETER);
			g2d.setStroke(new BasicStroke(3));
			g.setColor(borderColor);
			g.drawOval(x - DIAMETER/2, y - DIAMETER/2, DIAMETER, DIAMETER);
			g2d.setStroke(new BasicStroke(1));
		}
	}
}
