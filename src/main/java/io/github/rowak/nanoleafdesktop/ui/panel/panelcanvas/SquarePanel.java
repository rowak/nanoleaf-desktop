package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

public class SquarePanel extends PanelShape {
	
	public SquarePanel(int x, int y, int rotation) {
		super(rotation);
		
		//top-left
		addPoint(x, y);
		//top-right
		addPoint(x + 90, y);
		//bottom-right
		addPoint(x + 90, y + 90);
		//bottom-left
		addPoint(x, y + 90);
		
		rotate();
	}
}
