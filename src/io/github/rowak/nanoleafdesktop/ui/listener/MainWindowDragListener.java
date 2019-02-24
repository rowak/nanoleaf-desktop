package io.github.rowak.nanoleafdesktop.ui.listener;

import java.awt.event.MouseEvent;

import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class MainWindowDragListener extends WindowDragListener
{
	PanelCanvas canvas;
	
	public MainWindowDragListener(int titleBarWidth, PanelCanvas canvas)
	{
		super(titleBarWidth);
		this.canvas = canvas;
	}
	
	@Override
	public void mouseReleased(MouseEvent me)
	{
		super.mouseReleased(me);
		canvas.repaint();
	}
}
