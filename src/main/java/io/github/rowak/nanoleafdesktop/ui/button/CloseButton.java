package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import io.github.rowak.nanoleafdesktop.Main;

public class CloseButton extends JButton {
	
	public CloseButton(final Component frame, int operation) {
		URL iconPath =
				Main.class.getResource("/images/close_button_icon.png");
		URL iconHighlightedPath =
				Main.class.getResource("/images/close_button_icon_highlighted.png");
		setIcon(new ImageIcon(iconPath));
		setRolloverIcon(new ImageIcon(iconHighlightedPath));
		setPressedIcon(new ImageIcon(iconHighlightedPath));
		setContentAreaFilled(false);
		setBorder(null);
		addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (e.getButton() == MouseEvent.BUTTON1) {
            		if (operation == JFrame.EXIT_ON_CLOSE) {
            			System.exit(0);
            		}
            		else if (operation == JFrame.HIDE_ON_CLOSE) {
            			frame.setVisible(false);
            		}
            		// Only available for JDialog components
            		else if (operation == JFrame.DISPOSE_ON_CLOSE) {
            			((JDialog)frame).dispose();
            		}
            	}
            }
        });
	}
}
