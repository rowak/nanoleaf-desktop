package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JToggleButton;

public class ModernToggleButton extends JToggleButton {
	
	public ModernToggleButton() {
		init();
	}
	
	public ModernToggleButton(String text) {
		init();
		setText(text);
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
