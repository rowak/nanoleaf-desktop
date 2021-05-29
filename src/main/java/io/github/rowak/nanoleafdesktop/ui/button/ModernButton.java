package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ModernButton extends JButton {
	
	public ModernButton() {
		init();
	}
	
	public ModernButton(String text) {
		init();
		setText(text);
	}
	
	public ModernButton(ImageIcon unpressedIcon, ImageIcon pressedIcon) {
		init();
		setIcon(unpressedIcon);
		setPressedIcon(pressedIcon);
	}
	
	private void init() {
		setFont(new Font("Tahoma", Font.PLAIN, 20));
		setContentAreaFilled(false);
		setBackground(Color.DARK_GRAY);
		if (System.getProperty("os.name").equals("Mac OS X")) {
			setForeground(Color.BLACK);
		}
		else {
			setForeground(Color.WHITE);
		}
	}
}
