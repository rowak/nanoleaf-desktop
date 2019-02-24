package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import io.github.rowak.Panel;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;

public class PanelActionListener extends MouseAdapter
{
	private int lastXDiff;
	private Panel[] panels;
	private Map<Panel, Point> panelLocations, tempPanelLocations;
	private Point mouseLast;
	private PanelCanvas canvas;
	
	public PanelActionListener(PanelCanvas canvas,
			Panel[] panels, Map<Panel, Point> panelLocations)
	{
		this.canvas = canvas;
		this.panels = panels;
		this.panelLocations = panelLocations;
		tempPanelLocations = new HashMap<Panel, Point>(panelLocations);
	}
	
	private void movePanelsUsingMouse(Point mouse)
	{
		int xdiff = mouse.x - mouseLast.x;
		int ydiff = mouse.y - mouseLast.y;
		
		for (Panel p : panels)
		{
			int x = 0, y = 0;
			if (tempPanelLocations.get(p) != null)
			{
				x = tempPanelLocations.get(p).x + xdiff;
				y = tempPanelLocations.get(p).y + ydiff;
				panelLocations.put(p, new Point(x, y));
			}
		}
		canvas.repaint();
	}
	
	private void rotatePanelsUsingMouse(Point mouse)
	{
		int xdiff = (mouse.x - mouseLast.x)/5;
		int rotation = canvas.getRotation() + xdiff - lastXDiff;
		canvas.rotatePanels(rotation);
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
		mouseLast = e.getPoint();
		canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
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
		tempPanelLocations = new HashMap<Panel, Point>(panelLocations);
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
			else if (SwingUtilities.isRightMouseButton(e))
			{
				rotatePanelsUsingMouse(e.getPoint());
			}
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		scalePanelsUsingMouse(e.getWheelRotation());
	}
}
