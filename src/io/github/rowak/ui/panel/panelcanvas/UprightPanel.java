package io.github.rowak.ui.panel.panelcanvas;

public class UprightPanel extends PanelShape
{
	public UprightPanel(int x, int y, PanelCanvas canvas)
	{
		super(canvas);
		
		// top
		addPoint(x + 7, y -60);
		addPoint(x -7, y -60);
		// bottom-left
		addPoint(x -60, y + 40);
		addPoint(x -55, y + 50);
		// bottom-right
		addPoint(x + 55, y + 50);
		addPoint(x + 60, y + 40);
		
		rotate();
	}
}
