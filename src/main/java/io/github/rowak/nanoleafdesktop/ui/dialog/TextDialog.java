package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;

public class TextDialog extends BasicDialog {
	
	public TextDialog(Component parent, String text) {
		super();
		
		JLabel lblText = new JLabel(addLineBreaks(text));
		lblText.setFont(FONT);
		lblText.setForeground(Color.WHITE);
		contentPanel.add(lblText, "gapx 15 0, cell 0 1,alignx center,aligny bottom");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 3");
		
		finalize(parent, text);
	}
	
	private String addLineBreaks(String text) {
		return "<html>" + text.replace("\n", "<br>") + "</html>";
	}
	
	protected void finalize(Component parent, String text) {
		pack();
		int extraHeight = (text.split("<br>").length)*15;
		setSize(getWidth() + 15, getHeight() + extraHeight);
		setLocationRelativeTo(parent);
	}
}
