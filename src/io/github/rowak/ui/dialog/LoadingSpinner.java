package io.github.rowak.ui.dialog;

import java.awt.Color;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import io.github.rowak.Main;

public class LoadingSpinner extends JLabel
{
	private final URL FILE_PATH = Main.class.getResource("resources/images/loading_spinner.gif");
	
	public LoadingSpinner(Color background)
	{
		setIcon(new ImageIcon(FILE_PATH));
		setOpaque(true);
		setBackground(background);
		setHorizontalAlignment(JLabel.CENTER);
		setVerticalAlignment(JLabel.CENTER);
	}
}
