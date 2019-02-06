package io.github.rowak.ui.panel.panelcanvas;

public class InvertedPanel extends PanelShape
{
	public InvertedPanel(int x, int y, PanelCanvas canvas)
	{
		super(canvas);
		
		// top-left
		addPoint(x -55, y -15);
		addPoint(x -50, y -25);
		// top-right
		addPoint(x + 50, y -25);
		addPoint(x + 55, y -15);
		// bottom
		addPoint(x + 5, y + 80);
		addPoint(x -5, y + 80);
		
		rotate();
	}
}
