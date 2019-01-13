package io.github.rowak.ui.panel.panelcanvas;

import io.github.rowak.Frame;
import io.github.rowak.Panel;

public class PanelFrame
{
	private Panel panel;
	private Frame frame;
	
	public PanelFrame(Panel panel, Frame frame)
	{
		this.panel = panel;
		this.frame = frame;
	}
	
	public Panel getPanel()
	{
		return panel;
	}
	
	public Frame getFrame()
	{
		return frame;
	}
}
