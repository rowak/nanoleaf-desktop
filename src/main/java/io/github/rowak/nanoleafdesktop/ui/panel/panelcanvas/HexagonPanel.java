package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

public class HexagonPanel extends PanelShape {
	
	public static final int SIDE_LENGTH = 54;
	
	public HexagonPanel(int x, int y, int rotation) {
		super(rotation);
		
		//top-left
		addPoint(x - SIDE_LENGTH/2-2, y - SIDE_LENGTH);
		//top-right
		addPoint(x + SIDE_LENGTH/2+2, y - SIDE_LENGTH);
		//right
		addPoint(x + (SIDE_LENGTH+SIDE_LENGTH/6)-2, y);
		//bottom-right
		addPoint(x + SIDE_LENGTH/2+2, y + SIDE_LENGTH);
		//bottom-left
		addPoint(x - SIDE_LENGTH/2-2, y + SIDE_LENGTH);
		//left
		addPoint(x - (SIDE_LENGTH+SIDE_LENGTH/6)+2, y);
		
		rotate();
	}
}
