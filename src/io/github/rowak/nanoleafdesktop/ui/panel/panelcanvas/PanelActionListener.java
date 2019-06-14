package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import io.github.rowak.Aurora;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;

public class PanelActionListener extends MouseAdapter
{
	private int lastXDiff;
	private int deviceIndex;
	private Panel[][] panels, tempPanels;
	private Aurora[] devices;
	private Point mouseLast;
	private PanelCanvas canvas;
	
	public PanelActionListener(PanelCanvas canvas,
			Panel[][] panels, Aurora[] devices)
	{
		this.canvas = canvas;
		this.panels = panels;
		this.devices = devices;
		tempPanels = clonePanels(panels);
	}
	
	public Panel[][] getTempPanels()
	{
		return tempPanels;
	}
	
	public Panel[][] getPanels()
	{
		return panels;
	}
	
	private Panel[][] clonePanels(Panel[][] original)
	{
		Panel[][] temp = new Panel[original.length][];
		for (int i = 0; i < original.length; i++)
		{
			temp[i] = new Panel[original[i].length];
			for (int j = 0; j < original[i].length; j++)
			{
				Panel p = original[i][j];
				temp[i][j] = new Panel(p.getId(),
						p.getX(), p.getY(), p.getOrientation());
			}
		}
		return temp;
	}
	
	private void movePanelsUsingMouse(Point mouse)
	{
		if (deviceIndex != -1)
		{
			int xdiff = roundToNearest(mouse.x - mouseLast.x, 150f/2f);
			int ydiff = roundToNearest(mouse.y - mouseLast.y, 130f/2f);
			
			Point[] offset = canvas.getPanelOffset();
			offset[deviceIndex].setLocation(xdiff, ydiff);
			
			canvas.repaint();
		}
	}
	
	private void rotatePanelsUsingMouse(Point mouse)
	{
		int xdiff = (mouse.x - mouseLast.x)/5;
		int rotation = canvas.getRotation() + xdiff - lastXDiff;
		canvas.setRotation(rotation);
		lastXDiff = xdiff;
		canvas.repaint();
	}
	
	private void scalePanelsUsingMouse(int rotationdiff)
	{
		float scaleFactor = canvas.getScaleFactor();
		scaleFactor += rotationdiff * 0.05f;
		if (scaleFactor > 0)
		{
			canvas.setScaleFactor(scaleFactor);
			canvas.repaint();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (mouseLast == null)
		{
			mouseLast = e.getPoint();
			canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			deviceIndex = getDeviceIndex(e.getPoint());
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (lastXDiff != 0)
		{
			PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
			manager.setProperty("panelRotation", canvas.getRotation());
		}
		
		mouseLast = null;
		lastXDiff = 0;
		tempPanels = clonePanels(panels);
		if (deviceIndex != -1)
		{
			Point[] offset = canvas.getPanelOffset();
			final int di = deviceIndex;
			for (int i = 0; i < tempPanels[deviceIndex].length; i++)
			{
				tempPanels[di][i].setX(tempPanels[di][i].getX() + offset[di].x);
				tempPanels[di][i].setY(tempPanels[di][i].getY() + offset[di].y);
			}
			offset[deviceIndex] = new Point(0, 0);
			deviceIndex = -1;
		}
				
		panels = clonePanels(tempPanels);
		canvas.setPanels(panels);
		try
		{
			canvas.checkAuroraState();
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
		canvas.repaint();
		canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (mouseLast != null)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				movePanelsUsingMouse(e.getPoint());
			}
			// ROTATION DISABLED
//			else if (SwingUtilities.isRightMouseButton(e))
//			{
//				rotatePanelsUsingMouse(e.getPoint());
//			}
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		scalePanelsUsingMouse(e.getWheelRotation());
	}
	
	private int getDeviceIndex(Point mouse)
	{
		for (int i = 0; i < tempPanels.length; i++)
		{
			String deviceType = getDeviceType(devices[i]);
			for (Panel p : tempPanels[i])
			{
				PanelShape shape = null;
				if (deviceType.equals("aurora"))
				{
					int o = p.getOrientation();
					if (o == 0 || Math.abs(o) % 120 == 0)
					{
						shape = new UprightPanel(p.getX(),
								p.getY(), canvas);
					}
					else
					{
						shape = new InvertedPanel(p.getX(),
								p.getY(), canvas);
					}
				}
				else if (deviceType.equals("canvas"))
				{
					shape = new SquarePanel(p.getX(),
							p.getY(), canvas);
				}
				
				if (shape != null && shape.contains(mouse))
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	private String getDeviceType(Aurora device)
	{
		String name = device.getName().toLowerCase();
		if (name.contains("light panels") ||
				name.contains("aurora"))
		{
			return "aurora";
		}
		else if (name.contains("canvas"))
		{
			return "canvas";
		}
		return "";
	}
	
	private int roundToNearest(int num, float factor)
	{
		return (int)(Math.ceil(num / (float)factor) * factor);
	}
}
