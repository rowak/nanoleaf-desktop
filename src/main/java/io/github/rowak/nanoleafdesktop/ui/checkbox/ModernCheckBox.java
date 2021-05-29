package io.github.rowak.nanoleafdesktop.ui.checkbox;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ModernCheckBox extends JCheckBox {
	
	private final String CHECKBOX_UNCHECKED_ICON_PATH = "checkbox_unchecked_icon.png";
	private final String CHECKBOX_CHECKED_ICON_PATH = "checkbox_checked_icon.png";

	public ModernCheckBox() {
		initUI();
	}

	public ModernCheckBox(String text) {
		setText(text);
		setFont(UIConstants.largeLabelFont);
		setForeground(Color.WHITE);
		initUI();
	}

	private void initUI() {
		setBackground(new Color(0, 0, 0, 0));
		setIcon(pathToIcon(CHECKBOX_UNCHECKED_ICON_PATH));
		setSelectedIcon(pathToIcon(CHECKBOX_CHECKED_ICON_PATH));
	}

	private Icon pathToIcon(String path) {
		URL iconPath = Main.class.getResource("/images/" + path);
		return new ImageIcon(iconPath);
	}
}
