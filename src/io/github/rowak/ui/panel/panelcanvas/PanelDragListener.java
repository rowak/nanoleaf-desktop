package io.github.rowak.ui.panel.panelcanvas;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import io.github.rowak.Main;
import io.github.rowak.Panel;
import io.github.rowak.tools.PropertyManager;

public class PanelDragListener extends MouseAdapter
{
	private int lastXDiff;
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
		int xdiff = (mouse.x - mouseLast.x);
		int rotation = canvas.getRotation() + xdiff - lastXDiff;
		canvas.rotatePanels(rotation);
		lastXDiff = xdiff;
		canvas.repaint();
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
}
