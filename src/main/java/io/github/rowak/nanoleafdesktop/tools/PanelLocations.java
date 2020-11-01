package io.github.rowak.nanoleafdesktop.tools;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StatusCodeException;

public class PanelLocations
{
	private int[] rotation;
	private Panel[][] panels;
	
	public PanelLocations(Aurora[] devices) throws StatusCodeException
	{
		panels = new Panel[devices.length][];
		for (int i = 0; i < devices.length; i++)
		{
			panels[i] = devices[i].panelLayout().getPanelsRotated();
		}
		rotation = new int[devices.length];
		for (int i = 0; i < rotation.length; i++)
		{
			rotation[i] = 360 - devices[i].panelLayout().getGlobalOrientation();
		}
	}
	
	public Panel[][] getPanels()
	{
		return panels;
	}
	
	public void setPanels(Panel[][] panels)
	{
		this.panels = panels;
	}
	
	public int getRotation(int deviceIndex)
	{
		return rotation[deviceIndex];
	}
	
	public void setRotation(int rotation, int deviceIndex)
	{
		this.rotation[deviceIndex] = rotation;
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
	
	public Panel[] getGroupPanels()
	{
		List<Panel> groupPanels = new ArrayList<Panel>();
		for (int i = 0; i < panels.length; i++)
		{
			for (Panel p : panels[i])
			{
				int x = (p.getX()/* + panelOffset[i].x*/);
				int y = -(p.getY()/* + panelOffset[i].y*/);
				groupPanels.add(new Panel(p.getId(),
						x, y, p.getOrientation()));
			}
		}
		return groupPanels.toArray(new Panel[]{});
	}
	
	private double distanceToPanel(Point point, Panel panel)
	{
		int panelx = panel.getX();
		int panely = panel.getY();
		return point.distance(panelx, panely);
	}
}
