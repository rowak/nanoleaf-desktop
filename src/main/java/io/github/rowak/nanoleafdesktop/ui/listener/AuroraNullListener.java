package io.github.rowak.nanoleafdesktop.ui.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.DiscoveryPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.EffectsPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.InformationPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.KeyShortcutsPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.SpotifyPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

@Deprecated
public class AuroraNullListener extends Timer {
	
	public AuroraNullListener(int delay, ActionListener listener,
			InformationPanel infoPanel, PanelCanvas canvas,
			DiscoveryPanel discoveryPanel, AmbilightPanel ambilightPanel,
			SpotifyPanel spotifyPanel, KeyShortcutsPanel shortcutsPanel) {
		super(delay, listener);
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canvas.getAuroras() != null) {
					infoPanel.setAuroras(canvas.getAuroras());
					discoveryPanel.setAuroras(canvas.getAuroras());
					ambilightPanel.setAuroras(canvas.getAuroras());
					spotifyPanel.setAuroras(canvas.getAuroras());
					shortcutsPanel.setAuroras(canvas.getAuroras());
					canvas.initCanvas();
					canvas.repaint();
					
					AuroraNullListener.this.stop();
				}
			}
		});
	}
}
