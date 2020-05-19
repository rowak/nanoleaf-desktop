package io.github.rowak.nanoleafdesktop.ui.textfield;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class ModernTextField extends JTextField
{
	public ModernTextField()
	{
		init();
	}
	
	public ModernTextField(String defaultText)
	{
		init();
		setText(defaultText);
	}
	
	private void init()
	{
		setForeground(Color.WHITE);
		setBackground(Color.DARK_GRAY);
		setBorder(new LineBorder(Color.GRAY));
		setCaretColor(Color.WHITE);
		setFont(new Font("Tahoma", Font.PLAIN, 22));
	}
}
