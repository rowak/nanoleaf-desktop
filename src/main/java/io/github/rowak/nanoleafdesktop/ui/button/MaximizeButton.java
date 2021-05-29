package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import io.github.rowak.nanoleafdesktop.Main;

public class MaximizeButton extends JButton {
	
	private static URL maximizeIconPath = Main.class.getResource("/images/maximize_button_icon.png");
	private static URL maximizeIconHighlightedPath = Main.class.getResource("/images/maximize_button_icon_highlighted.png");
	private static URL windowedIconPath = Main.class.getResource("/images/windowed_button_icon.png");
	private static URL windowedIconHighlightedPath = Main.class.getResource("/images/windowed_button_icon_highlighted.png");
	
	public MaximizeButton(final JFrame frame) {
//		setText((char)0x25A1 + "");
		//setText("\u20de");
		setIcon(new ImageIcon(maximizeIconPath));
		setRolloverIcon(new ImageIcon(maximizeIconHighlightedPath));
		setPressedIcon(new ImageIcon(maximizeIconHighlightedPath));
		setContentAreaFilled(false);
		setBorder(null);
		addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (e.getButton() == MouseEvent.BUTTON1) {
            		if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            			frame.setExtendedState(JFrame.NORMAL);
            			setIcon(new ImageIcon(maximizeIconPath));
            			setRolloverIcon(new ImageIcon(maximizeIconHighlightedPath));
            			setPressedIcon(new ImageIcon(maximizeIconHighlightedPath));
            		}
            		else if (frame.getExtendedState() == JFrame.NORMAL) {
            			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            			setIcon(new ImageIcon(windowedIconPath));
            			setRolloverIcon(new ImageIcon(windowedIconHighlightedPath));
            			setPressedIcon(new ImageIcon(windowedIconHighlightedPath));
            		}
            	}
            }
        });
	}
}
