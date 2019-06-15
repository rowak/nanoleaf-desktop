package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.Point;
import java.awt.Polygon;

public class PanelShape extends Polygon
{
	private int rotation;
	
	public PanelShape(int rotation)
	{
		this.rotation = rotation;
	}
	
	protected void rotate()
	{
		Point origin = getOrigin();
		
		for (int i = 0; i < npoints; i++)
		{
			double radAngle = Math.toRadians(rotation);
			
			xpoints[i] -= origin.x;
			ypoints[i] -= origin.y;
			
			double newX = xpoints[i] * Math.cos(radAngle) - ypoints[i] * Math.sin(radAngle);
			double newY = xpoints[i] * Math.sin(radAngle) + ypoints[i] * Math.cos(radAngle);
			
			xpoints[i] = (int)(newX + origin.x);
			ypoints[i] = (int)(newY + origin.y);
		}
	}
	
	private Point getOrigin()
	{
		int originX = 0, originY = 0;
		for (int i = 0; i < npoints; i++)
		{
			originX += xpoints[i];
			originY += ypoints[i];
		}
		originX /= npoints;
		originY /= npoints;
		return new Point(originX, originY);
	}
}
