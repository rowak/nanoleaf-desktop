package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

public class InvertedPanel extends PanelShape
{
	public InvertedPanel(int x, int y, int rotation)
	{
		super(rotation);
		
		// top-left
		addPoint(x -59, y -23);
		addPoint(x -51, y -37);
		// top-right
		addPoint(x + 51, y -37);
		addPoint(x + 59, y -23);
		// bottom
		addPoint(x + 7, y + 64);
		addPoint(x -7, y + 64);
		
		rotate();
	}
}
