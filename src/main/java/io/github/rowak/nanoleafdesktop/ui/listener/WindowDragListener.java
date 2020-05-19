package io.github.rowak.nanoleafdesktop.ui.listener;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

public class WindowDragListener extends MouseInputAdapter
{
	int titleBarWidth;
	Point location;
	MouseEvent pressed;
	
	public WindowDragListener(int titleBarWidth)
	{
		this.titleBarWidth = titleBarWidth;
	}

	public void mousePressed(MouseEvent me)
	{
		if (me.getY() < titleBarWidth)
		{
			pressed = me;
		}
	}
	
	public void mouseReleased(MouseEvent me)
	{
		pressed = null;
	}

	public void mouseDragged(MouseEvent me)
	{
		if (pressed != null)
		{
			Component component = me.getComponent();
			location = component.getLocation(location);
			int x = location.x - pressed.getX() + me.getX();
			int y = location.y - pressed.getY() + me.getY();
			component.setLocation(x, y);
		}
	}
}
