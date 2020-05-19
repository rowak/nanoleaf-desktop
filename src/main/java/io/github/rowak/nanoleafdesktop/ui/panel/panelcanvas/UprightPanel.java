package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

public class UprightPanel extends PanelShape
{
	public UprightPanel(int x, int y, int rotation)
	{
		super(rotation);
		
		// top
		addPoint(x + 7, y -64);
		addPoint(x -7, y -64);
		// bottom-left
		addPoint(x -59, y + 23);
		addPoint(x -51, y + 37);
		// bottom-right
		addPoint(x + 51, y + 37);
		addPoint(x + 59, y + 23);
		
		rotate();
	}
}
