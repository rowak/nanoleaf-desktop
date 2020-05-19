package io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker;

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

public class BrightnessSlider extends JPanel
{
	private int width, height;
	private SliderTracker tracker;
	
	public BrightnessSlider(int width, int height)
	{
		this.width = width;
		this.height = height;
		add(Box.createRigidArea(new Dimension(width, height)));
		setBackground(Color.DARK_GRAY);
		tracker = new SliderTracker();
	}
	
	public int getValue()
	{
		return getValue(tracker.getY());
	}
	
	public void setValue(int value)
	{
		tracker.setY(value);
	}
	
	public void setColor(Color color)
	{
		float[] hsbVals = new float[3];
		hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(),
				color.getBlue(), hsbVals);
		int value = (int)(hsbVals[2]*100f);
		tracker.setY(value);
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
	
	private int getValue(int height)
	{
		height = this.height - height;
		return (int)((height/(float)this.height)*100);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		paintGradient(g);
		g.setColor(Color.GRAY);
		g.drawRect(0, 0, width, height);
		tracker.paint(g);
	}
	
	public void paintGradient(Graphics g)
	{
		int THICKNESS = height/100;
		
		for (int y = 0; y < 100; y++)
		{
			g.setColor(Color.getHSBColor(0, 0, y/100f));
			g.fillRect(0, height - y*THICKNESS, width, THICKNESS);
		}
	}
	
	private class SliderTracker
	{
		private int y;
		private Color borderColor, trackerColor;
		
		public SliderTracker()
		{
			this.y = height/2;
			borderColor = Color.DARK_GRAY;
			trackerColor = Color.WHITE;

			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					updatePosition(e.getY());
				}
			});
			addMouseMotionListener(new MouseAdapter()
			{
				@Override
				public void mouseDragged(MouseEvent e)
				{
					updatePosition(e.getY());
				}
			});
		}
		
		private void updatePosition(int y)
		{
			if (y > 0 && y < height)
			{
				this.y = y;
				fireChangeListeners();
				repaint();
			}
		}
		
		public int getY()
		{
			return this.y;
		}
		
		public void setY(int y)
		{
			int THICKNESS = height/100;
			this.y = height - y*THICKNESS;
			repaint();
		}
		
		public void paint(Graphics g)
		{
			final int THICKNESS = 10;
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(trackerColor);
			g.fillRect(0, y - THICKNESS/2, width, THICKNESS);
			g2d.setStroke(new BasicStroke(3));
			g.setColor(borderColor);
			g.drawRect(0, y - THICKNESS/2, width, THICKNESS);
			g2d.setStroke(new BasicStroke(1));
		}
	}
}
