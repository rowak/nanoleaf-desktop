package io.github.rowak.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import io.github.rowak.ui.panel.DiscoveryPanel;
import io.github.rowak.ui.panel.Ambilight.AmbilightPanel;
import io.github.rowak.ui.panel.panelcanvas.PanelCanvas;

public class AuroraNullListener extends Timer
{
	public AuroraNullListener(int delay, ActionListener listener,
			PanelCanvas canvas, DiscoveryPanel discoveryPanel, AmbilightPanel ambilightPanel)
	{
		super(delay, listener);
		
		addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (canvas.getAurora() != null)
				{
					discoveryPanel.setAurora(canvas.getAurora());
					ambilightPanel.setAurora(canvas.getAurora());
					canvas.initCanvas();
					canvas.repaint();
					
					AuroraNullListener.this.stop();
				}
			}
		});
	}
}
