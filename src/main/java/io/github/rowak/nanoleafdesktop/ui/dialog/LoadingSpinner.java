package io.github.rowak.nanoleafdesktop.ui.dialog;

import io.github.rowak.nanoleafdesktop.Main;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class LoadingSpinner extends JLabel {
	private final URL FILE_PATH = Main.class.getResource("/images/loading_spinner.gif");

	public LoadingSpinner(Color background) {
		setIcon(new ImageIcon(FILE_PATH));
		setOpaque(true);
		setBackground(background);
		setHorizontalAlignment(JLabel.CENTER);
		setVerticalAlignment(JLabel.CENTER);
	}
}
